package com.whylock.whylock.controller;

import com.whylock.whylock.dto.ScanRequest;
import com.whylock.whylock.model.AiScanResponse;
import com.whylock.whylock.model.ScanHistory;
import com.whylock.whylock.service.AiScanOrchestrator;
import com.whylock.whylock.service.AiService;
import com.whylock.whylock.service.RateLimitService;
import com.whylock.whylock.service.ScanHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scan")
public class AiScanController {

    private final AiScanOrchestrator orchestrator;
    private final ScanHistoryService scanHistoryService;
    private final RateLimitService rateLimitService;
    private final AiService geminiAiService;

    public AiScanController(AiScanOrchestrator orchestrator,
                            ScanHistoryService scanHistoryService,
                            RateLimitService rateLimitService,
                            AiService geminiAiService) {
        this.orchestrator = orchestrator;
        this.scanHistoryService = scanHistoryService;
        this.rateLimitService = rateLimitService;
        this.geminiAiService = geminiAiService;
    }

    /**
     * POST /api/scan/ai-analyze
     * Rate limited: 1000 per minute, 10000 per hour per user.
     */
    @PostMapping("/ai-analyze")
    public ResponseEntity<?> aiAnalyze(
            @RequestBody ScanRequest request,
            Principal principal) {

        if (request.getTargetUrl() == null || request.getTargetUrl().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "targetUrl is required"));
        }

        String username = principal != null ? principal.getName() : "anonymous";

        if (!rateLimitService.tryConsume(username)) {
            long remaining = rateLimitService.getAvailableTokens(username);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "error",      "Rate limit exceeded",
                            "message",    "Max 1000 scans per minute, 10000 per hour",
                            "remaining",  remaining,
                            "retryAfter", "60 seconds"
                    ));
        }

        AiScanResponse response = orchestrator.runFullAiScan(
                request.getTargetUrl(), username);

        long remaining = rateLimitService.getAvailableTokens(username);
        return ResponseEntity.ok()
                .header("X-RateLimit-Limit-Minute", "1000")
                .header("X-RateLimit-Limit-Hour", "10000")
                .header("X-RateLimit-Remaining", String.valueOf(remaining))
                .body(response);
    }

    /**
     * DELETE /api/scan/cache?url=https://example.com
     * Clears Redis cache for a URL — forces fresh Gemini scan next time.
     */
    @DeleteMapping("/cache")
    public ResponseEntity<Map<String, String>> evictCache(
            @RequestParam String url) {
        geminiAiService.evictCache(url);
        return ResponseEntity.ok(Map.of(
                "message", "Cache cleared for: " + url,
                "url",     url
        ));
    }

    /**
     * GET /api/scan/history
     */
    @GetMapping("/history")
    public ResponseEntity<List<ScanHistory>> getHistory(Principal principal) {
        return ResponseEntity.ok(
                scanHistoryService.getHistoryForUser(principal.getName())
        );
    }

    /**
     * GET /api/scan/history/url?target=https://example.com
     */
    @GetMapping("/history/url")
    public ResponseEntity<List<ScanHistory>> getHistoryForUrl(
            @RequestParam String target,
            Principal principal) {
        return ResponseEntity.ok(
                scanHistoryService.getHistoryForUrl(principal.getName(), target)
        );
    }

    /**
     * GET /api/scan/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ScanHistoryService.UserStats> getStats(
            Principal principal) {
        return ResponseEntity.ok(
                scanHistoryService.getUserStats(principal.getName())
        );
    }

    /**
     * GET /api/scan/quota
     */
    @GetMapping("/quota")
    public ResponseEntity<Map<String, Object>> getQuota(Principal principal) {
        String username = principal.getName();
        long remaining = rateLimitService.getAvailableTokens(username);
        return ResponseEntity.ok(Map.of(
                "username",       username,
                "remaining",      remaining,
                "limitPerMinute", 1000,
                "limitPerHour",   10000
        ));
    }

    /**
     * GET /api/scan/ai-status
     */
    @GetMapping("/ai-status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok(
                "WHYLOCK AI Scanner running — Gemini 2.5 Flash | " +
                        "Rate limit: 1000/min, 10000/hr | Scan history + Redis cache enabled"
        );
    }
}