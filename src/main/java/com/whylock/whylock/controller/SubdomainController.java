package com.whylock.whylock.controller;

import com.whylock.whylock.model.SubdomainResult;
import com.whylock.whylock.scanner.SubdomainScannerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subdomain")
public class SubdomainController {

    private final SubdomainScannerService subdomainScannerService;

    public SubdomainController(SubdomainScannerService subdomainScannerService) {
        this.subdomainScannerService = subdomainScannerService;
    }

    /**
     * POST /api/subdomain/scan
     * Scans all common subdomains for a given domain.
     *
     * Body: { "domain": "google.com" }
     *
     * Returns list of all subdomains found with:
     * - IP address
     * - alive/dead status
     * - SSL status
     * - risk score per subdomain
     */
    @PostMapping("/scan")
    public ResponseEntity<?> scanSubdomains(@RequestBody Map<String, String> body) {
        String domain = body.get("domain");
        if (domain == null || domain.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "domain is required"));
        }

        List<SubdomainResult> results = subdomainScannerService.scanSubdomains(domain);

        long alive = results.stream().filter(SubdomainResult::isAlive).count();
        long risky = results.stream()
                .filter(r -> r.isAlive() &&
                        ("HIGH".equals(r.getRiskLevel()) || "CRITICAL".equals(r.getRiskLevel())))
                .count();

        return ResponseEntity.ok(Map.of(
                "domain",        domain,
                "totalChecked",  results.size(),
                "alive",         alive,
                "riskySubdomains", risky,
                "results",       results
        ));
    }

    /**
     * GET /api/subdomain/status
     */
    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok(
                "WHYLOCK Subdomain Scanner — checks 38 common subdomains in parallel"
        );
    }
}