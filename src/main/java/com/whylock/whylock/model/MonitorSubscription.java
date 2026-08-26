package com.whylock.whylock.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "monitor_subscriptions")
public class MonitorSubscription {
    @Id
    private String id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "target_url", nullable = false)
    private String targetUrl;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "check_interval_minutes")
    private int checkIntervalMinutes = 60;

    @Column(name = "last_checked")
    private LocalDateTime lastChecked;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "last_risk_score")
    private int lastRiskScore = -1;

    @Column(name = "last_critical_count")
    private int lastCriticalCount = 0;

    @Column(name = "last_scanned_at")
    private LocalDateTime lastScannedAt;

    @Column(name = "unsubscribe_token")
    private String unsubscribeToken;

    public MonitorSubscription() {}

    public MonitorSubscription(Long userId, String targetUrl, String email) {
        this.userId = userId;
        this.targetUrl = targetUrl;
        this.url = targetUrl;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getCheckIntervalMinutes() { return checkIntervalMinutes; }
    public void setCheckIntervalMinutes(int checkIntervalMinutes) { this.checkIntervalMinutes = checkIntervalMinutes; }

    public LocalDateTime getLastChecked() { return lastChecked; }
    public void setLastChecked(LocalDateTime lastChecked) { this.lastChecked = lastChecked; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

    public int getLastRiskScore() { return lastRiskScore; }
    public void setLastRiskScore(int lastRiskScore) { this.lastRiskScore = lastRiskScore; }

    public int getLastCriticalCount() { return lastCriticalCount; }
    public void setLastCriticalCount(int lastCriticalCount) { this.lastCriticalCount = lastCriticalCount; }

    public LocalDateTime getLastScannedAt() { return lastScannedAt; }
    public void setLastScannedAt(LocalDateTime lastScannedAt) { this.lastScannedAt = lastScannedAt; }

    public String getUnsubscribeToken() { return unsubscribeToken; }
    public void setUnsubscribeToken(String unsubscribeToken) { this.unsubscribeToken = unsubscribeToken; }
}
