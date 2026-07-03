package com.whylock.whylock.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whylock.whylock.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    @Value("${groq.api.key}")
    private String apiKey;

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // FIX: "unless" prevents error/empty responses from ever being cached.
    // A single Groq hiccup (503, timeout, bad JSON) used to poison the cache
    // for that URL permanently — every future scan, including hourly
    // scheduled rescans, kept replaying that same empty result forever.
    @Cacheable(
            value = "scanResults",
            key = "#url",
            unless = "#result == null " +
                    "or #result.overallRisk == 'ERROR' " +
                    "or #result.overallRisk == 'PARSE_ERROR' " +
                    "or #result.findings == null " +
                    "or #result.findings.isEmpty()"
    )
    public AiScanResponse analyze(String url,
                                  Map<String, Object> scanData,
                                  Map<String, String> osintData,
                                  List<Map<String, Object>> cveData) {
        try {
            String prompt = buildPrompt(url, scanData, osintData, cveData);
            String aiResponse = callGroq(prompt);
            AiScanResponse result = parseResponse(url, aiResponse);
            if (result.getFindings() == null || result.getFindings().isEmpty()) {
                log.warn("analyze({}) parsed successfully but returned 0 findings — raw AI response follows:\n{}",
                        url, aiResponse);
            }
            return result;
        } catch (Exception e) {
            log.error("analyze({}) failed — Groq call or parsing threw: {}", url, e.getMessage(), e);
            return buildErrorResponse(url, e.getMessage());
        }
    }

    @CacheEvict(value = "scanResults", key = "#url")
    public void evictCache(String url) {
        log.info("Cache evicted for: {}", url);
    }

    private String buildPrompt(String url,
                               Map<String, Object> scanData,
                               Map<String, String> osintData,
                               List<Map<String, Object>> cveData) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are WHYLOCK, an expert cybersecurity AI. ");
        sb.append("Analyze the following URL security scan data and think step by step.\n\n");
        sb.append("## URL Being Analyzed\n").append(url).append("\n\n");
        sb.append("## HTTP Security Scan Results\n").append(scanData).append("\n\n");
        sb.append("## OSINT / DNS / Network Metadata\n").append(osintData).append("\n\n");
        sb.append("## Global CVE Threat Intelligence (NIST NVD)\n").append(cveData).append("\n\n");
        sb.append("## Instructions\n");
        sb.append("1. Think through each security signal carefully.\n");
        sb.append("2. Cross-reference the scan data against known CVEs.\n");
        sb.append("3. Identify ALL security problems.\n");
        sb.append("4. For each problem, generate the EXACT fix code.\n");
        sb.append("5. Match problems to specific CVE IDs where relevant.\n");
        sb.append("Keep each fixCode under 5 lines. Keep descriptions under 2 sentences.\n\n");
        sb.append("## Required Output Format (strict JSON, no extra text)\n");
        sb.append("{\n");
        sb.append("  \"overallRisk\": \"CRITICAL|HIGH|MEDIUM|LOW|SAFE\",\n");
        sb.append("  \"riskScore\": 0-100,\n");
        sb.append("  \"summary\": \"plain English summary for a CISO\",\n");
        sb.append("  \"findings\": [\n");
        sb.append("    {\n");
        sb.append("      \"title\": \"short problem name\",\n");
        sb.append("      \"severity\": \"CRITICAL|HIGH|MEDIUM|LOW\",\n");
        sb.append("      \"description\": \"what this means and why it matters\",\n");
        sb.append("      \"fixCode\": \"exact code or config to fix this\",\n");
        sb.append("      \"cveMatch\": \"CVE-XXXX-XXXXX or null\"\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"globalThreats\": [\"list of relevant world-level threats\"]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String callGroq(String prompt) throws Exception {
        int retries = 3;
        for (int attempt = 1; attempt <= retries; attempt++) {
            String requestBody = mapper.writeValueAsString(Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.2,
                    "max_tokens", 4096
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = mapper.readTree(response.body());
                return root.path("choices").path(0)
                        .path("message").path("content").asText();
            }

            if ((response.statusCode() == 503 || response.statusCode() == 429) && attempt < retries) {
                long waitMs = 2000L * attempt;
                log.warn("Groq {} — retry {}/{} in {}ms", response.statusCode(), attempt, retries, waitMs);
                Thread.sleep(waitMs);
                continue;
            }

            throw new RuntimeException("Groq API error: " + response.statusCode()
                    + " — " + response.body());
        }
        throw new RuntimeException("Groq unavailable after " + retries + " retries");
    }

    private AiScanResponse parseResponse(String url, String rawText) {

        AiScanResponse response = new AiScanResponse();

        response.setUrl(url);
        response.setAiModel("llama3-70b (Groq free tier)");
        response.setScannedAt(LocalDateTime.now());

        try {

            String json = rawText.trim();

            if (json.startsWith("```")) {
                json = json
                        .replaceAll("```json\\n?", "")
                        .replaceAll("```\\n?", "")
                        .trim();
            }

            JsonNode root = mapper.readTree(json);

            response.setOverallRisk(
                    root.path("overallRisk").asText("UNKNOWN")
            );

            response.setRiskScore(
                    root.path("riskScore").asInt(0)
            );

            response.setSummary(
                    root.path("summary")
                            .asText("No summary generated.")
            );

            List<AiScanFinding> findings = new ArrayList<>();

            int critical = 0;
            int high = 0;
            int medium = 0;
            int low = 0;

            for (JsonNode f : root.path("findings")) {

                String severity =
                        f.path("severity")
                                .asText("LOW")
                                .toUpperCase();

                findings.add(

                        new AiScanFinding(

                                f.path("title").asText(),

                                severity,

                                f.path("description").asText(),

                                f.path("fixCode").asText(),

                                f.path("cveMatch").asText(null)

                        )

                );

                switch (severity) {

                    case "CRITICAL":
                        critical++;
                        break;

                    case "HIGH":
                        high++;
                        break;

                    case "MEDIUM":
                        medium++;
                        break;

                    default:
                        low++;
                        break;
                }

            }

            response.setFindings(findings);
            List<VulnerabilityDetail> vulnerabilities = new ArrayList<>();

            for (AiScanFinding finding : findings) {

                VulnerabilityDetail v = new VulnerabilityDetail();

                v.setTitle(finding.getTitle());
                v.setSeverity(finding.getSeverity());
                v.setDescription(finding.getDescription());
                v.setRemediation(finding.getFixCode());
                v.setCveId(finding.getCveMatch());

                vulnerabilities.add(v);
            }

            response.setVulnerabilities(vulnerabilities);

            response.setTotalVulnerabilities(findings.size());

            response.setCriticalCount(critical);

            response.setHighCount(high);

            response.setMediumCount(medium);

            response.setLowCount(low);

            List<String> threats = new ArrayList<>();

            for (JsonNode t : root.path("globalThreats")) {

                threats.add(t.asText());

            }

            response.setGlobalThreats(threats);

            response.setSslValid(true);

// SSL
            SslReport ssl = new SslReport();
            ssl.setCaValid(true);
            ssl.setTls13Supported(true);
            ssl.setTls12Supported(true);
            ssl.setLegacyTlsEnabled(false);
            ssl.setHstsEnabled(true);
            ssl.setCipherStrong(true);

            response.setSslReport(ssl);

// Response Time
            response.setResponseTime(150);

// Tech Stack
            List<TechStackItem> tech = new ArrayList<>();

            TechStackItem item = new TechStackItem();
            item.setLayer("Backend");
            item.setName("Spring Boot");
            item.setVersion("3.x");
            item.setConfidence(100);
            item.setCveCount(0);

            tech.add(item);

            response.setTechStack(tech);


        }

        catch (Exception e) {

            log.error("parseResponse({}) — could not parse Groq output as JSON: {}\nRaw response:\n{}",
                    url, e.getMessage(), rawText);

            response.setOverallRisk("PARSE_ERROR");

            response.setRiskScore(0);

            response.setSummary(
                    "AI JSON Parse Error : " + e.getMessage()
            );

            response.setFindings(new ArrayList<>());

            response.setGlobalThreats(new ArrayList<>());

            response.setTotalVulnerabilities(0);

            response.setCriticalCount(0);

            response.setHighCount(0);

            response.setMediumCount(0);

            response.setLowCount(0);

            response.setSslValid(false);

            response.setResponseTime(0);

            response.setVulnerabilities(new ArrayList<>());

            response.setTechStack(new ArrayList<>());

        }

        return response;
    }
    private AiScanResponse buildErrorResponse(String url, String error) {
        AiScanResponse response = new AiScanResponse();
        response.setUrl(url);
        response.setOverallRisk("ERROR");
        response.setSummary("AI analysis failed: " + error);
        response.setFindings(new ArrayList<>());
        response.setGlobalThreats(new ArrayList<>());
        response.setAiModel("llama3-70b (Groq free tier)");
        response.setScannedAt(LocalDateTime.now());
        return response;
    }
}