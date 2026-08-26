package com.whylock.whylock.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scan_history")
public class ScanHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    private String overallRisk;

    @Column(nullable = false)
    private int riskScore;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false)
    private int findingsCount;

    @Column(nullable = false)
    private String aiModel;

    @Column(nullable = false)
    private LocalDateTime scannedAt;

    @Column(columnDefinition = "TEXT")
    private String fullResponse;

    public ScanHistory() {
        this.scannedAt = LocalDateTime.now();
    }

    public ScanHistory(
            User user,
            String url,
            String overallRisk,
            int riskScore,
            String summary,
            int findingsCount,
            String aiModel,
            String fullResponse
    ) {
        this.user = user;
        this.url = url;
        this.overallRisk = overallRisk;
        this.riskScore = riskScore;
        this.summary = summary;
        this.findingsCount = findingsCount;
        this.aiModel = aiModel;
        this.fullResponse = fullResponse;
        this.scannedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOverallRisk() {
        return overallRisk;
    }

    public void setOverallRisk(String overallRisk) {
        this.overallRisk = overallRisk;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getFindingsCount() {
        return findingsCount;
    }

    public void setFindingsCount(int findingsCount) {
        this.findingsCount = findingsCount;
    }

    public String getAiModel() {
        return aiModel;
    }

    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }

    public LocalDateTime getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(LocalDateTime scannedAt) {
        this.scannedAt = scannedAt;
    }

    public String getFullResponse() {
        return fullResponse;
    }

    public void setFullResponse(String fullResponse) {
        this.fullResponse = fullResponse;
    }
}