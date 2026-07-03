package com.whylock.whylock.controller;

import com.whylock.whylock.model.LoginRequest;
import com.whylock.whylock.model.RiskResponse;
import com.whylock.whylock.service.RiskService;
import com.whylock.whylock.audit.AuditLog;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/risk")
public class RiskController {

    private final RiskService riskService;

    // ✅ Constructor Injection
    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    // ✅ Test endpoint
    @GetMapping("/")
    public String home() {
        return "WHYLOCK is running 🚀";
    }

    // ✅ Main Risk Evaluation API (FIXED)
    @PostMapping("/evaluate")
    public RiskResponse evaluate(@RequestBody LoginRequest request) {
        return riskService.evaluateRisk(request);  // ✅ matches your service
    }

    // ✅ Get Audit Logs
    @GetMapping("/audit")
    public List<AuditLog> getAuditLogs() {
        return riskService.getAuditLogs();
    }
}