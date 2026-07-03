package com.whylock.whylock.controller;

import com.whylock.whylock.threat.CveEntry;
import com.whylock.whylock.threat.CveFeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * WHYLOCK CVE Controller
 *
 * Exposes global threat intelligence data to the WHYLOCK API.
 *
 * Endpoints:
 *  GET /api/cve/status        → feed health + counts
 *  GET /api/cve/all           → all CVEs (real or mock)
 *  GET /api/cve/critical      → CRITICAL severity only
 *  GET /api/cve/exploited     → actively exploited only
 */
@RestController
@RequestMapping("/api/cve")
public class CveController {

    private final CveFeedService cveFeedService;

    public CveController(CveFeedService cveFeedService) {
        this.cveFeedService = cveFeedService;
    }

    // Feed health — show at start of presentation
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(cveFeedService.getStatus());
    }

    // All CVEs
    @GetMapping("/all")
    public ResponseEntity<List<CveEntry>> getAllCves() {
        return ResponseEntity.ok(cveFeedService.getAllCves());
    }

    // Critical only
    @GetMapping("/critical")
    public ResponseEntity<List<CveEntry>> getCritical() {
        return ResponseEntity.ok(cveFeedService.getCriticalCves());
    }

    // Actively exploited right now
    @GetMapping("/exploited")
    public ResponseEntity<List<CveEntry>> getExploited() {
        return ResponseEntity.ok(cveFeedService.getActivelyExploited());
    }
}