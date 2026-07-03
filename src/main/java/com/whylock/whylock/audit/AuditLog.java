package com.whylock.whylock.audit;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID decisionId;

    private String username;
    private int trustScore;
    private String action;
    private LocalDateTime timestamp;

    @ElementCollection
    private List<String> reasons;

    public AuditLog() {}

    public AuditLog(String username, int trustScore, String action, List<String> reasons) {
        this.username = username;
        this.trustScore = trustScore;
        this.action = action;
        this.reasons = reasons;
        this.timestamp = LocalDateTime.now();
    }

    public UUID getDecisionId() { return decisionId; }
    public String getUsername() { return username; }
    public int getTrustScore() { return trustScore; }
    public String getAction() { return action; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public List<String> getReasons() { return reasons; }
}