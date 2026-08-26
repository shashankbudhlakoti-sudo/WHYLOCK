package com.whylock.whylock.scheduler;

import com.whylock.whylock.model.ScanHistory;
import com.whylock.whylock.model.User;
import com.whylock.whylock.repository.UserRepository;
import com.whylock.whylock.service.AiScanOrchestrator;
import com.whylock.whylock.service.AiService;
import com.whylock.whylock.service.ScanHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class HourlyRescanScheduler {

    private static final Logger log = LoggerFactory.getLogger(HourlyRescanScheduler.class);

    private final UserRepository userRepository;
    private final ScanHistoryService scanHistoryService;
    private final AiScanOrchestrator orchestrator;
    private final AiService aiService;

    public HourlyRescanScheduler(
            UserRepository userRepository,
            ScanHistoryService scanHistoryService,
            AiScanOrchestrator orchestrator,
            AiService aiService) {
        this.userRepository = userRepository;
        this.scanHistoryService = scanHistoryService;
        this.orchestrator = orchestrator;
        this.aiService = aiService;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void rescanAndEmailLastUrls() {
        List<User> users = userRepository.findAll();
        log.info("Hourly rescan started for {} user(s)", users.size());

        for (User user : users) {
            try {
                List<ScanHistory> history =
                        scanHistoryService.getHistoryForUser(user.getUsername());

                if (history == null || history.isEmpty()) {
                    log.warn("Hourly rescan: skipping {} — no scan history found", user.getUsername());
                    continue;
                }

                String lastUrl = history.get(0).getUrl();
                if (lastUrl == null || lastUrl.isBlank()) {
                    log.warn("Hourly rescan: skipping {} — latest scan has blank URL", user.getUsername());
                    continue;
                }

                aiService.evictCache(lastUrl);

                log.info("Hourly rescan: {} -> {}", user.getUsername(), lastUrl);
                orchestrator.runFullAiScan(lastUrl, user.getUsername());

                TimeUnit.SECONDS.sleep(3);

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.warn("Hourly rescan interrupted");
                break;
            } catch (Exception e) {
                log.warn("Hourly rescan failed for {}: {}", user.getUsername(), e.getMessage());
            }
        }

        log.info("Hourly rescan completed");
    }
}