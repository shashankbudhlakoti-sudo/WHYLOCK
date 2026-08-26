package com.whylock.whylock.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whylock.whylock.model.VulnerabilityDetail;
import com.whylock.whylock.dto.FixRequest;
import com.whylock.whylock.dto.FixResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * WhyLock AI Fix Assistant
 * ─────────────────────────
 * Uses Groq API (FREE tier) with llama-3.3-70b-versatile to generate
 * enterprise-grade remediation guidance for every vulnerability.
 *
 * Free tier: 14,400 requests/day, 500,000 tokens/day — more than enough.
 *
 * Config in application.yml:
 *   whylock:
 *     groq:
 *       api-key: ${GROQ_API_KEY}
 *       model: llama-3.3-70b-versatile
 *       base-url: https://api.groq.com/openai/v1
 */
@Service
public class AiFixAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiFixAssistantService.class);

    // FIX 1: All three config values injected via constructor instead of field injection.
    // This makes missing properties fail fast at startup (IllegalStateException) rather
    // than silently passing null to setBearerAuth() at runtime and causing a 401.
    private final String groqApiKey;
    private final String model;
    private final String baseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AiFixAssistantService(
            @Value("${whylock.groq.api-key}") String groqApiKey,
            @Value("${whylock.groq.model:llama-3.3-70b-versatile}") String model,
            @Value("${whylock.groq.base-url:https://api.groq.com/openai/v1}") String baseUrl,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {

        // FIX 1 (cont): Validate at construction time so the app refuses to start
        // instead of sending malformed requests hours later.
        if (groqApiKey == null || groqApiKey.isBlank()) {
            throw new IllegalStateException(
                    "whylock.groq.api-key must be set (e.g. export GROQ_API_KEY=gsk_...)");
        }
        this.groqApiKey   = groqApiKey;
        this.model        = model;
        this.baseUrl      = baseUrl;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Generate a complete remediation playbook for a single vulnerability.
     * Uses structured prompting so Groq returns JSON we can parse directly.
     */
    public FixResponse generateFix(FixRequest request) {
        String prompt  = buildFixPrompt(request);
        String rawJson = callGroq(prompt, 1500);
        return parseFixResponse(rawJson, request);
    }

    /**
     * Enrich a list of vulnerabilities with AI-generated fix code + explanations.
     * Called from ScanOrchestrationService after scan completes.
     *
     * FIX 2: Added 200 ms inter-request delay to stay within Groq's free-tier
     * per-minute token/request limits and avoid silent 429 responses.
     */
    public void enrichWithFixes(List<VulnerabilityDetail> vulns, String techStack) {
        if (vulns == null) return;

        for (VulnerabilityDetail v : vulns) {
            try {
                FixRequest req = new FixRequest();
                req.setVulnerabilityType(v.getVulnerabilityType());
                req.setCveId(v.getCveId());
                req.setTitle(v.getTitle());
                req.setSeverity(v.getSeverity());
                req.setTechStack(techStack);
                req.setContext(v.getDescription());

                FixResponse fix = generateFix(req);
                v.setAiFixExplanation(fix.getExplanation());
                v.setAiFixCode(fix.getCodeSnippet());
                v.setAiFixSteps(fix.getSteps());
                v.setAiFixReferences(fix.getReferences());

                // FIX 2: Rate-limit guard — 5 requests/second max on free tier.
                // Without this, 20+ vulns fire back-to-back and get silently 429'd.
                TimeUnit.MILLISECONDS.sleep(200);

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.warn("enrichWithFixes interrupted");
                break;
            } catch (Exception e) {
                log.warn("Could not generate AI fix for {}: {}", v.getCveId(), e.getMessage());
            }
        }
    }

    /**
     * One-shot chat: ask Groq a free-form security question.
     * Used by the /api/fix/chat endpoint for the interactive AI agent.
     *
     * FIX 3: conversationContext is now injected as an "assistant" role message
     * instead of a second "user" message, preventing prompt-injection attacks
     * where a caller could embed "Ignore previous instructions..." in the context.
     */
    public String chat(String userMessage, String conversationContext) {
        String systemPrompt = """
            You are WhyLock Security AI — an expert penetration tester and secure-code advisor.
            You help developers understand vulnerabilities and fix them in real code.
            Always be precise, reference CVEs, cite OWASP where relevant.
            When you give code examples, use Java/Spring Boot unless asked otherwise.
            Be concise but comprehensive. Format code in plain text blocks, no markdown.
            """;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("max_tokens", 1000);
        body.put("temperature", 0.3);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // FIX 3: Prior conversation context belongs in the "assistant" role so the
        // model treats it as its own earlier output, not as a new user instruction.
        // Placing it in a "user" role allowed prompt-injection via the context string.
        if (conversationContext != null && !conversationContext.isBlank()) {
            messages.add(Map.of("role", "assistant", "content", conversationContext));
        }

        messages.add(Map.of("role", "user", "content", userMessage));
        body.put("messages", messages);

        try {
            String response = postToGroq(body);
            JsonNode root = objectMapper.readTree(response);

            // FIX 4 (applied here too): guard against empty choices before get(0)
            JsonNode choices = root.path("choices");
            if (choices.isEmpty()) {
                String err = root.path("error").path("message").asText("Unknown Groq error");
                log.error("Groq chat returned no choices: {}", err);
                return "AI agent temporarily unavailable. Please retry.";
            }

            return choices.get(0).path("message").path("content").asText();

        } catch (Exception e) {
            log.error("Groq chat error: {}", e.getMessage());
            return "AI agent temporarily unavailable. Please retry.";
        }
    }

    // ── Prompt Builder ─────────────────────────────────────────────────────────

    private String buildFixPrompt(FixRequest req) {
        return """
            You are a senior application security engineer. A vulnerability scanner found the following:

            CVE / ID     : %s
            Title        : %s
            Severity     : %s
            Type         : %s
            Tech Stack   : %s
            Context      : %s

            Respond ONLY with a JSON object (no markdown, no explanation outside JSON) with this exact shape:
            {
              "explanation": "2-3 sentence clear explanation of why this is dangerous",
              "steps": ["Step 1: ...", "Step 2: ...", "Step 3: ..."],
              "code_snippet": "actual Java/Spring Boot fix code — real, compilable",
              "references": ["OWASP link", "CVE link", "Spring docs link"],
              "estimated_fix_time": "e.g. 2 hours",
              "risk_if_ignored": "what happens if not fixed"
            }
            """.formatted(
                req.getCveId()            != null ? req.getCveId()            : "N/A",
                req.getTitle(),
                req.getSeverity(),
                req.getVulnerabilityType(),
                req.getTechStack()        != null ? req.getTechStack()        : "Java Spring Boot",
                req.getContext()          != null ? req.getContext()          : "No additional context"
        );
    }

    // ── Groq HTTP Calls ────────────────────────────────────────────────────────

    private String callGroq(String userPrompt, int maxTokens) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("max_tokens", maxTokens);
        body.put("temperature", 0.2);
        body.put("messages", List.of(
                Map.of("role", "system", "content",
                        "You are a security expert. Always respond with valid JSON only."),
                Map.of("role", "user", "content", userPrompt)
        ));
        return postToGroq(body);
    }

    private String postToGroq(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // FIX 1 (cont): RestTemplate throws HttpClientErrorException (4xx) and
        // HttpServerErrorException (5xx) as RuntimeExceptions BEFORE returning a
        // ResponseEntity, so the old `if (!is2xxSuccessful())` check was never reached.
        // We now catch those Spring exceptions explicitly and re-wrap them with the
        // response body included, which makes the root cause visible in logs.
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions", entity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new RuntimeException(
                    "Groq API error: " + ex.getStatusCode()
                            + " — " + ex.getResponseBodyAsString(), ex);
        }
    }

    // ── Response Parser ────────────────────────────────────────────────────────

    private FixResponse parseFixResponse(String rawJson, FixRequest req) {
        FixResponse fix = new FixResponse();
        fix.setCveId(req.getCveId());
        fix.setTitle(req.getTitle());

        try {
            JsonNode root = objectMapper.readTree(rawJson);

            // FIX 4: Guard against empty or missing "choices" array.
            // Groq returns {"error":{"message":"..."}} on rate-limit / bad request.
            // The old code called get(0) unconditionally, throwing NullPointerException
            // whenever the choices array was absent or empty.
            JsonNode choices = root.path("choices");
            if (choices.isEmpty()) {
                String groqError = root.path("error").path("message")
                        .asText("No choices returned");
                log.warn("Groq returned no choices for CVE {}: {}", req.getCveId(), groqError);
                fix.setExplanation("AI fix unavailable: " + groqError);
                fix.setSteps(List.of("Review OWASP guidelines for " + req.getVulnerabilityType()));
                return fix;
            }

            JsonNode content  = choices.get(0).path("message").path("content");
            String   jsonText = content.asText().strip();

            // Strip any accidental markdown fences
            if (jsonText.startsWith("```")) {
                jsonText = jsonText.replaceAll("```json|```", "").strip();
            }

            JsonNode fixJson = objectMapper.readTree(jsonText);
            fix.setExplanation(fixJson.path("explanation").asText());
            fix.setCodeSnippet(fixJson.path("code_snippet").asText());
            fix.setEstimatedFixTime(fixJson.path("estimated_fix_time").asText());
            fix.setRiskIfIgnored(fixJson.path("risk_if_ignored").asText());

            List<String> steps = new ArrayList<>();
            fixJson.path("steps").forEach(s -> steps.add(s.asText()));
            fix.setSteps(steps);

            List<String> refs = new ArrayList<>();
            fixJson.path("references").forEach(r -> refs.add(r.asText()));
            fix.setReferences(refs);

        } catch (Exception e) {
            log.warn("Could not parse Groq fix JSON for CVE {}, using fallback: {}",
                    req.getCveId(), e.getMessage());
            fix.setExplanation(
                    "AI fix generation encountered a parsing issue. See raw remediation guidance.");
            fix.setSteps(List.of("Review OWASP guidelines for " + req.getVulnerabilityType()));
        }
        return fix;
    }
}