package com.whylock.whylock.controller;

import com.whylock.whylock.model.PortScanResult;
import com.whylock.whylock.scanner.PortScannerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/portscan")
public class PortScanController {

    private final PortScannerService portScannerService;

    public PortScanController(PortScannerService portScannerService) {
        this.portScannerService = portScannerService;
    }

    /**
     * POST /api/portscan/scan
     * Scans 30 common ports for a target host.
     *
     * Body: { "target": "google.com" }
     *
     * Returns:
     * - All open ports with service name
     * - Risk level per port
     * - Summary of dangerous open ports
     * - Overall port risk assessment
     */
    @PostMapping("/scan")
    public ResponseEntity<?> scanPorts(@RequestBody Map<String, String> body) {
        String target = body.get("target");
        if (target == null || target.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "target is required"));
        }

        List<PortScanResult> results = portScannerService.scanPorts(target);

        // Summarize
        List<PortScanResult> openPorts = results.stream()
                .filter(PortScanResult::isOpen)
                .collect(Collectors.toList());

        List<PortScanResult> criticalOpen = openPorts.stream()
                .filter(p -> "CRITICAL".equals(p.getRiskLevel()))
                .collect(Collectors.toList());

        List<PortScanResult> highOpen = openPorts.stream()
                .filter(p -> "HIGH".equals(p.getRiskLevel()))
                .collect(Collectors.toList());

        // Overall risk based on open ports
        String overallRisk;
        if (!criticalOpen.isEmpty())      overallRisk = "CRITICAL";
        else if (!highOpen.isEmpty())     overallRisk = "HIGH";
        else if (!openPorts.isEmpty())    overallRisk = "MEDIUM";
        else                              overallRisk = "LOW";

        return ResponseEntity.ok(Map.of(
                "target",           target,
                "totalChecked",     results.size(),
                "openPorts",        openPorts.size(),
                "criticalPorts",    criticalOpen.size(),
                "highRiskPorts",    highOpen.size(),
                "overallRisk",      overallRisk,
                "results",          results,
                "dangerousPorts",   criticalOpen.stream()
                        .map(p -> p.getPort() + " (" + p.getService() + ")")
                        .collect(Collectors.toList())
        ));
    }

    /**
     * GET /api/portscan/status
     */
    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok(
                "WHYLOCK Port Scanner — checks 30 common ports in parallel"
        );
    }
}