package com.whylock.whylock.service;

import com.whylock.whylock.model.LoginRequest;
import com.whylock.whylock.model.RiskResponse;
import com.whylock.whylock.repository.AuditLogRepository;
import com.whylock.whylock.audit.AuditLog;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RiskService {

    private Map<String, List<Integer>> riskHistory = new ConcurrentHashMap<>();
    private final AuditLogRepository auditLogRepository;

    public RiskService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public RiskResponse evaluateRisk(LoginRequest request) {

        int risk = 0;
        List<String> reasons = new ArrayList<>();

        // ── Signal evaluation ─────────────────────────────────────
        if (request.isSuspiciousIp()) {
            risk += 40;
            reasons.add("Suspicious IP detected");
        }

        if (!request.isKnownDevice()) {
            risk += 25;
            reasons.add("Unknown device");
        }

        if (request.isUnusualTime()) {
            risk += 20;
            reasons.add("Unusual login time");
        }

        if (request.getFailedAttempts() >= 3) {
            risk += 35;
            reasons.add("Multiple failed login attempts");
        }

        if (request.getAmount() > 500000) {
            risk += 40;
            reasons.add("High transaction amount");
        }

        if (request.isNewDevice()) {
            risk += 20;
            reasons.add("New device behavior detected");
        }

        // ── FIX 1: cap risk at 100 before any calculation ─────────
        risk = Math.min(risk, 100);

        // ── FIX 2: trustScore now always 0–100 ────────────────────
        int trustScore = 100 - risk;

        // ── History tracking ──────────────────────────────────────
        riskHistory.putIfAbsent(request.getUsername(), new ArrayList<>());
        List<Integer> history = riskHistory.get(request.getUsername());
        history.add(trustScore);
        if (history.size() > 20) history.remove(0);

        double volatility = calculateVolatility(history);

        // ── Risk level + action ───────────────────────────────────
        String riskLevel;
        String action;

        if (trustScore >= 70) {
            riskLevel = "LOW";
            action = "ALLOW";
        } else if (trustScore >= 40) {
            riskLevel = "MEDIUM";
            action = "REQUIRE_MFA";
        } else {
            riskLevel = "HIGH";
            action = "BLOCK";
        }

        // ── Velocity check — rapid trust drop ────────────────────
        if (history.size() >= 2) {
            int previous = history.get(history.size() - 2);
            int velocity = trustScore - previous;
            if (velocity < -25) {
                riskLevel = "CRITICAL";
                action = "BLOCK_IMMEDIATELY";
                reasons.add("Rapid trust decline detected");
            }
        }

        // ── Volatility check — erratic behaviour ─────────────────
        if (volatility > 20) {
            riskLevel = "UNSTABLE";
            action = "STEP_UP_VERIFICATION";
            reasons.add("Behavioral volatility detected");
        }

        // ── FIX 3: confidenceScore now always 0.0–1.0 ────────────
        double confidenceScore = calculateConfidence(risk);

        // ── Persist to audit log ──────────────────────────────────
        AuditLog log = new AuditLog(
                request.getUsername(),
                trustScore,
                action,
                reasons
        );
        auditLogRepository.save(log);

        return new RiskResponse(
                trustScore,
                riskLevel,
                action,
                confidenceScore,
                reasons,
                volatility
        );
    }

    // ── confidence: 1.0 = no risk, 0.0 = maximum risk ────────────
    private double calculateConfidence(int risk) {
        return 1.0 - (risk / 100.0);
    }

    // ── volatility: std deviation of trust score history ─────────
    private double calculateVolatility(List<Integer> history) {
        if (history.size() < 2) return 0.0;

        double mean = history.stream()
                .mapToInt(i -> i)
                .average()
                .orElse(0.0);

        double variance = history.stream()
                .mapToDouble(i -> Math.pow(i - mean, 2))
                .average()
                .orElse(0.0);

        return Math.sqrt(variance);
    }

    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAll();
    }
}