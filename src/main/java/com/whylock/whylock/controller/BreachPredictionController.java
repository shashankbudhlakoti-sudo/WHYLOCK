package com.whylock.whylock.controller;

import com.whylock.whylock.model.BreachPrediction;
import com.whylock.whylock.service.BreachPredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/breach")
public class BreachPredictionController {

    private final BreachPredictionService breachPredictionService;

    public BreachPredictionController(BreachPredictionService breachPredictionService) {
        this.breachPredictionService = breachPredictionService;
    }

    /**
     * POST /api/breach/predict
     * Predicts how attackers will exploit a site's vulnerabilities.
     *
     * Body:
     * {
     *   "url": "https://example.com",
     *   "overallRisk": "HIGH",
     *   "riskScore": 72,
     *   "findings": ["Missing HSTS", "No CSP header"],
     *   "openPorts": ["80 (HTTP)", "3306 (MySQL)"]
     * }
     */
    @PostMapping("/predict")
    public ResponseEntity<?> predict(@RequestBody Map<String, Object> body) {
        String url = (String) body.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "url is required"));
        }

        String overallRisk = (String) body.getOrDefault("overallRisk", "UNKNOWN");
        int riskScore = (int) body.getOrDefault("riskScore", 0);

        @SuppressWarnings("unchecked")
        List<String> findings = (List<String>) body.getOrDefault("findings", List.of());

        @SuppressWarnings("unchecked")
        List<String> openPorts = (List<String>) body.getOrDefault("openPorts", List.of());

        BreachPrediction prediction = breachPredictionService.predict(
                url, overallRisk, riskScore, findings, openPorts);

        return ResponseEntity.ok(prediction);
    }

    /**
     * GET /api/breach/status
     */
    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok(
                "WHYLOCK Breach Prediction — powered by Gemini AI"
        );
    }
}