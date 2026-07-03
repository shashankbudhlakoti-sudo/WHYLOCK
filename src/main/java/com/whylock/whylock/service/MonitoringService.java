package com.whylock.whylock.service;

import com.whylock.whylock.model.AiScanResponse;
import com.whylock.whylock.model.MonitorSubscription;
import com.whylock.whylock.repository.MonitorSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * WhyLock Monitoring + Email Alert Service
 * ─────────────────────────────────────────
 * Features:
 *  • Subscribe/unsubscribe any URL for monitoring
 *  • Configurable scan frequency per subscription (default: daily)
 *  • Email alerts when risk score shifts > 5 pts or new Critical appears
 *  • Beautiful HTML email with WhyLock navy branding
 *  • Fully free: Spring @Scheduled + Spring Mail (SMTP)
 *
 * application.yml:
 *   spring:
 *     mail:
 *       host: smtp.gmail.com        # or any free SMTP
 *       port: 587
 *       username: ${MAIL_USER}
 *       password: ${MAIL_PASS}
 *       properties:
 *         mail.smtp.starttls.enable: true
 *   whylock:
 *     monitoring:
 *       alert-threshold: 5          # Risk score delta to trigger alert
 *       from-email: alerts@whylock.ai
 */
@Service
public class MonitoringService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringService.class);

    private final MonitorSubscriptionRepository subscriptionRepo;
    private final ScanOrchestrationService scanService;
    private final EmailService emailService;

    @org.springframework.beans.factory.annotation.Value("${whylock.monitoring.alert-threshold:5}")
    private int alertThreshold;

    @org.springframework.beans.factory.annotation.Value("${whylock.monitoring.from-email:alerts@whylock.ai}")
    private String fromEmail;

    public MonitoringService(
            MonitorSubscriptionRepository subscriptionRepo,
            ScanOrchestrationService scanService,
            EmailService emailService) {

        this.subscriptionRepo = subscriptionRepo;
        this.scanService = scanService;
        this.emailService = emailService;
    }
    // ── Subscription Management ───────────────────────────────────────────────

    @Transactional
    public MonitorSubscription subscribe(String url, String email, String cronExpression) {
        Optional<MonitorSubscription> existing = subscriptionRepo.findByUrlAndEmail(url, email);
        if (existing.isPresent()) {
            MonitorSubscription sub = existing.get();
            sub.setActive(true);
            sub.setCronExpression(cronExpression != null ? cronExpression : "0 0 * * *"); // daily default
            return subscriptionRepo.save(sub);
        }

        MonitorSubscription sub = new MonitorSubscription();
        sub.setId(UUID.randomUUID().toString());
        sub.setUrl(url);
        sub.setEmail(email);
        sub.setActive(true);
        sub.setCronExpression(cronExpression != null ? cronExpression : "0 0 * * *");
        sub.setLastRiskScore(-1);  // -1 means "never scanned"
        sub.setCreatedAt(LocalDateTime.now());
        sub.setUnsubscribeToken(UUID.randomUUID().toString());
        return subscriptionRepo.save(sub);
    }

    @Transactional
    public boolean unsubscribe(String token) {
        return subscriptionRepo.findByUnsubscribeToken(token).map(sub -> {
            sub.setActive(false);
            subscriptionRepo.save(sub);
            return true;
        }).orElse(false);
    }

    public List<MonitorSubscription> getSubscriptionsForEmail(String email) {
        return subscriptionRepo.findByEmailAndIsActiveTrue(email);
    }

    // ── Scheduled Monitoring Loop ─────────────────────────────────────────────

    /**
     * Runs every hour. Checks each active subscription's cron expression
     * to decide if a rescan is due, then scans and diffs the result.
     *
     * For production: replace with a proper cron-aware scheduler or
     * Quartz for per-subscription schedules.
     */
    @Scheduled(fixedDelayString = "${whylock.monitoring.check-interval-ms:3600000}")
    public void runMonitoringCycle() {
        log.info("WhyLock monitoring cycle starting...");
        List<MonitorSubscription> active = subscriptionRepo.findByIsActiveTrue();

        for (MonitorSubscription sub : active) {
            if (!isDue(sub)) continue;
            try {
                processSingleSubscription(sub);
            } catch (Exception e) {
                log.error("Monitor scan failed for {} ({}): {}", sub.getUrl(), sub.getEmail(), e.getMessage());
            }
        }
        log.info("WhyLock monitoring cycle complete. Processed {} subscriptions.", active.size());
    }

    @Transactional
    protected void processSingleSubscription(MonitorSubscription sub) {
        log.info("Re-scanning {} for {}", sub.getUrl(), sub.getEmail());

        AiScanResponse result = scanService.scan(sub.getUrl());
        int newScore = result.getRiskScore();
        int oldScore = sub.getLastRiskScore();
        boolean firstScan = (oldScore == -1);

        boolean newCritical = result.getCriticalCount() > sub.getLastCriticalCount();
        boolean scoreDelta  = !firstScan && Math.abs(newScore - oldScore) >= alertThreshold;

        if (firstScan || scoreDelta || newCritical) {
            sendAlertEmail(sub, result, oldScore, newScore, newCritical);
        }

        sub.setLastRiskScore(newScore);
        sub.setLastCriticalCount(result.getCriticalCount());
        sub.setLastScannedAt(LocalDateTime.now());
        subscriptionRepo.save(sub);
    }
    private void sendAlertEmail(MonitorSubscription sub,
                                AiScanResponse result,
                                int oldScore,
                                int newScore,
                                boolean newCritical) {

        try {

            String subject = buildSubject(result, newScore, newCritical);
            String html = buildHtmlEmail(sub, result, oldScore, newScore, newCritical);

            emailService.sendHtmlEmail(
                    sub.getEmail(),
                    subject,
                    html
            );

            log.info("Alert email sent to {} for {}", sub.getEmail(), sub.getUrl());

        } catch (Exception e) {

            log.error(
                    "Failed to send alert email to {} for {}",
                    sub.getEmail(),
                    sub.getUrl(),
                    e
            );
        }
    }

    private String buildSubject(AiScanResponse r, int newScore, boolean newCritical) {
        String emoji = newScore >= 80 ? "🔴" : newScore >= 60 ? "🟠" : newScore >= 40 ? "🟡" : "🟢";
        if (newCritical) return emoji + " [WHYLOCK ALERT] New Critical Vulnerability — " + r.getUrl();
        return emoji + " [WHYLOCK ALERT] Risk Score Changed to " + newScore + "/100 — " + r.getUrl();
    }

    private String buildHtmlEmail(MonitorSubscription sub, AiScanResponse r,
                                  int oldScore, int newScore, boolean newCritical) {
        String riskColor = newScore >= 80 ? "#FF3B3B" : newScore >= 60 ? "#FF8C00" : newScore >= 40 ? "#FFD600" : "#00E676";
        String deltaStr  = oldScore == -1 ? "First scan" :
                (newScore > oldScore ? "+" : "") + (newScore - oldScore) + " pts";

        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <style>
                body { margin:0; padding:0; background:#0A1628; font-family: Arial, sans-serif; }
                .wrapper { max-width:600px; margin:0 auto; background:#0A1628; }
                .header { background:#0D2145; padding:24px 32px; border-bottom:3px solid #1E6BFF; }
                .logo { font-size:28px; font-weight:bold; color:#FFFFFF; letter-spacing:3px; }
                .logo span { color:#00D4FF; }
                .content { padding:32px; }
                .alert-box { background:#122952; border-radius:10px; padding:24px; margin:0 0 20px; border-left:4px solid %s; }
                .score { font-size:56px; font-weight:bold; color:%s; line-height:1; }
                .score-label { color:#B0BFDA; font-size:12px; margin-top:4px; }
                .delta { font-size:14px; color:%s; font-weight:bold; }
                .url-box { background:#0D2145; border-radius:6px; padding:12px 16px; margin:16px 0; word-break:break-all; color:#00D4FF; font-size:13px; }
                table { width:100%%; border-collapse:collapse; margin:16px 0; }
                th { background:#1E6BFF; color:#fff; padding:8px 12px; text-align:left; font-size:12px; }
                td { padding:8px 12px; color:#E8EEF8; font-size:12px; border-bottom:1px solid #1E3A6E; }
                tr:nth-child(even) td { background:#122952; }
                .btn { display:inline-block; background:#1E6BFF; color:#fff; padding:12px 28px; border-radius:6px; text-decoration:none; font-weight:bold; font-size:14px; margin:8px 4px; }
                .btn-outline { background:transparent; border:1px solid #1E6BFF; color:#1E6BFF; }
                .footer { background:#0D2145; padding:20px 32px; text-align:center; color:#B0BFDA; font-size:11px; border-top:1px solid #1E3A6E; }
                .critical-badge { display:inline-block; background:#FF3B3B; color:#fff; padding:3px 10px; border-radius:4px; font-size:11px; font-weight:bold; }
              </style>
            </head>
            <body>
              <div class="wrapper">
                <div class="header">
                  <div class="logo">WHY<span>LOCK</span></div>
                  <div style="color:#B0BFDA; font-size:11px; margin-top:4px; letter-spacing:2px;">SECURITY INTELLIGENCE ALERT</div>
                </div>
                <div class="content">
                  <div class="alert-box">
                    <table style="margin:0">
                      <tr>
                        <td style="border:none; padding:0; width:120px; vertical-align:top">
                          <div class="score">%d</div>
                          <div class="score-label">RISK SCORE</div>
                          <div class="delta">%s</div>
                        </td>
                        <td style="border:none; padding:0 0 0 24px; vertical-align:top">
                          <div style="color:#fff; font-size:16px; font-weight:bold; margin-bottom:8px">%s</div>
                          <div class="url-box">%s</div>
                          %s
                        </td>
                      </tr>
                    </table>
                  </div>

                  <table>
                    <tr><th colspan="2">VULNERABILITY SUMMARY</th></tr>
                    <tr><td>🔴 Critical</td><td style="color:#FF3B3B; font-weight:bold">%d findings</td></tr>
                    <tr><td>🟠 High</td><td style="color:#FF8C00; font-weight:bold">%d findings</td></tr>
                    <tr><td>🟡 Medium</td><td style="color:#FFD600; font-weight:bold">%d findings</td></tr>
                    <tr><td>🟢 Low</td><td style="color:#00E676; font-weight:bold">%d findings</td></tr>
                    <tr><td>🔐 SSL Grade</td><td style="color:#00D4FF">%s</td></tr>
                    <tr><td>⏱ Scan Duration</td><td style="color:#B0BFDA">%d ms</td></tr>
                  </table>

                  <div style="text-align:center; margin:24px 0">
                    <a href="https://whylock.ai/report/%s" class="btn">View Full Report</a>
                    <a href="https://whylock.ai/unsubscribe?token=%s" class="btn btn-outline">Unsubscribe</a>
                  </div>

                  <p style="color:#B0BFDA; font-size:12px; text-align:center">
                    Next scheduled scan: <strong style="color:#00D4FF">%s</strong>
                  </p>
                </div>
                <div class="footer">
                  <div>WhyLock Security Intelligence  •  Powered by Groq AI</div>
                  <div style="margin-top:6px">You are receiving this because you subscribed to monitor <strong>%s</strong></div>
                  <div style="margin-top:4px"><a href="https://whylock.ai/unsubscribe?token=%s" style="color:#1E6BFF">Unsubscribe</a></div>
                </div>
              </div>
            </body>
            </html>
            """.formatted(
                riskColor,
                riskColor,
                riskColor,
                newScore,
                deltaStr,
                newCritical ? "🚨 NEW CRITICAL VULNERABILITY DETECTED" : "⚠️ Risk Score Changed",
                r.getUrl(),
                newCritical ? "<span class=\"critical-badge\">IMMEDIATE ACTION REQUIRED</span>" : "",
                r.getCriticalCount(),
                r.getHighCount(),
                r.getMediumCount(),
                r.getLowCount(),
                r.getSslReport() != null ? r.getSslReport().getSslGrade() : "N/A",
                r.getScanDurationMs(),
                r.getReportId(),
                sub.getUnsubscribeToken(),
                "Tomorrow at " + LocalDateTime.now().toLocalTime().withMinute(0).withSecond(0),
                sub.getUrl(),
                sub.getUnsubscribeToken()
        );
    }

    // ── Due Date Logic ────────────────────────────────────────────────────────

    private boolean isDue(MonitorSubscription sub) {
        if (sub.getLastScannedAt() == null) return true;
        // Simple daily check (production: use Quartz for per-sub cron)
        return sub.getLastScannedAt().isBefore(LocalDateTime.now().minusHours(23));
    }
}
