package com.whylock.whylock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whylock.whylock.model.AiScanResponse;
import com.whylock.whylock.model.ScanHistory;
import com.whylock.whylock.model.User;
import com.whylock.whylock.repository.ScanHistoryRepository;
import com.whylock.whylock.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScanHistoryService {

    private final ScanHistoryRepository repository;
    private final UserRepository userRepository;
    private final ObjectMapper mapper;

    public ScanHistoryService(
            ScanHistoryRepository repository,
            UserRepository userRepository,
            ObjectMapper mapper
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    /**
     * Save a completed AI scan to the database.
     */
    public ScanHistory save(String username, AiScanResponse response) {

        try {

            User user = userRepository
                    .findByUsername(username)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found: " + username
                            ));

            String fullJson = mapper.writeValueAsString(response);

            ScanHistory history = new ScanHistory(
                    user,
                    response.getUrl(),
                    response.getOverallRisk(),
                    response.getRiskScore(),
                    response.getSummary(),
                    response.getFindings() != null
                            ? response.getFindings().size()
                            : 0,
                    response.getAiModel(),
                    fullJson
            );

            return repository.save(history);

        } catch (Exception e) {

            System.err.println(
                    "Failed to save scan history: "
                            + e.getMessage()
            );

            return null;
        }
    }

    /**
     * Get all scans for a user.
     */
    public List<ScanHistory> getHistoryForUser(String username) {

        return repository
                .findByUserUsernameOrderByScannedAtDesc(
                        username
                );
    }

    /**
     * Get scans for a specific URL by a user.
     */
    public List<ScanHistory> getHistoryForUrl(
            String username,
            String url
    ) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + username
                        ));

        return repository
                .findByUserAndUrlOrderByScannedAtDesc(
                        user,
                        url
                );
    }

    /**
     * Global riskiest scans.
     */
    public List<ScanHistory> getGlobalRiskiest() {

        return repository
                .findTop10ByOrderByRiskScoreDesc();
    }

    /**
     * User statistics.
     */
    public UserStats getUserStats(String username) {

        long total =
                repository.countByUserUsername(
                        username
                );

        Double avg =
                repository.averageRiskScoreByUsername(
                        username
                );

        return new UserStats(
                total,
                avg != null ? avg : 0.0
        );
    }

    public static class UserStats {

        public long totalScans;
        public double averageRiskScore;

        public UserStats(
                long total,
                double avg
        ) {
            this.totalScans = total;
            this.averageRiskScore =
                    Math.round(avg * 10.0) / 10.0;
        }
    }
}