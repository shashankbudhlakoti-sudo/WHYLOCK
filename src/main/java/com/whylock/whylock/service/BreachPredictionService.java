package com.whylock.whylock.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whylock.whylock.model.BreachPrediction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BreachPredictionService {

    @Value("${groq.api.key}")
    private String apiKey;

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public BreachPrediction predict(String url,
                                    String overallRisk,
                                    int riskScore,
                                    List<String> findings,
                                    List<String> openPorts) {
        try {
            String prompt = buildPrompt(url, overallRisk, riskScore, findings, openPorts);
            String response = callGroq(prompt);
            return parseResponse(url, response);
        } catch (Exception e) {
            return buildErrorResponse(url, e.getMessage());
        }
    }

    private String buildPrompt(String url, String risk, int score,
                               List<String> findings, List<String> ports) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a world-class penetration tester and threat intelligence expert.\n\n");
        sb.append("Target URL: ").append(url).append("\n");
        sb.append("Current Risk Level: ").append(risk).append("\n");
        sb.append("Risk Score: ").append(score).append("/100\n");
        sb.append("Security Findings: ").append(findings).append("\n");
        sb.append("Open Ports: ").append(ports).append("\n\n");
        sb.append("Based on this data, predict:\n");
        sb.append("1. Top 5 most likely attack vectors attackers will use\n");
        sb.append("2. Probability of each attack (HIGH/MEDIUM/LOW)\n");
        sb.append("3. Which CVEs are most relevant\n");
        sb.append("4. Estimated time to breach if unpatched\n");
        sb.append("5. Priority fix order\n\n");
        sb.append("Respond ONLY in this strict JSON format:\n");
        sb.append("{\n");
        sb.append("  \"breachProbability\": \"HIGH|MEDIUM|LOW\",\n");
        sb.append("  \"estimatedTimeToBreach\": \"e.g. 24 hours, 7 days, 30 days\",\n");
        sb.append("  \"attackVectors\": [\n");
        sb.append("    {\n");
        sb.append("      \"name\": \"attack name\",\n");
        sb.append("      \"probability\": \"HIGH|MEDIUM|LOW\",\n");
        sb.append("      \"description\": \"how attacker will execute this\",\n");
        sb.append("      \"cve\": \"CVE-ID or null\",\n");
        sb.append("      \"prevention\": \"one line fix\"\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"priorityFixes\": [\"fix 1\", \"fix 2\", \"fix 3\"],\n");
        sb.append("  \"summary\": \"one paragraph for a CISO\"\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String callGroq(String prompt) throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.3,
                "max_tokens", 4096
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new RuntimeException("Groq error: " + response.statusCode()
                    + " — " + response.body());

        JsonNode root = mapper.readTree(response.body());
        return root.path("choices").path(0)
                .path("message").path("content").asText();
    }

    private BreachPrediction parseResponse(String url, String raw) {
        BreachPrediction prediction = new BreachPrediction();
        prediction.setUrl(url);
        try {
            String json = raw.trim()
                    .replaceAll("```json\\n?", "")
                    .replaceAll("```\\n?", "").trim();

            JsonNode root = mapper.readTree(json);
            prediction.setBreachProbability(root.path("breachProbability").asText());
            prediction.setEstimatedTimeToBreach(root.path("estimatedTimeToBreach").asText());
            prediction.setSummary(root.path("summary").asText());

            List<Map<String, String>> vectors = new ArrayList<>();
            for (JsonNode v : root.path("attackVectors")) {
                Map<String, String> vector = new java.util.LinkedHashMap<>();
                vector.put("name",        v.path("name").asText());
                vector.put("probability", v.path("probability").asText());
                vector.put("description", v.path("description").asText());
                vector.put("cve",         v.path("cve").asText("null"));
                vector.put("prevention",  v.path("prevention").asText());
                vectors.add(vector);
            }
            prediction.setAttackVectors(vectors);

            List<String> fixes = new ArrayList<>();
            for (JsonNode f : root.path("priorityFixes")) fixes.add(f.asText());
            prediction.setPriorityFixes(fixes);

        } catch (Exception e) {
            prediction.setBreachProbability("PARSE_ERROR");
            prediction.setSummary("Parse failed: " + e.getMessage());
        }
        return prediction;
    }

    private BreachPrediction buildErrorResponse(String url, String error) {
        BreachPrediction p = new BreachPrediction();
        p.setUrl(url);
        p.setBreachProbability("ERROR");
        p.setSummary("Prediction failed: " + error);
        return p;
    }
}