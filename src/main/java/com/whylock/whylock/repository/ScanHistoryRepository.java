package com.whylock.whylock.repository;

import com.whylock.whylock.model.ScanHistory;
import com.whylock.whylock.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScanHistoryRepository extends JpaRepository<ScanHistory, Long> {

    // All scans by a user, newest first
    List<ScanHistory> findByUserOrderByScannedAtDesc(User user);

    // Scans for a specific URL by a user
    List<ScanHistory> findByUserAndUrlOrderByScannedAtDesc(User user, String url);

    // Alternative: query directly by username
    List<ScanHistory> findByUserUsernameOrderByScannedAtDesc(String username);

    // All scans for a specific URL (any user)
    List<ScanHistory> findByUrlOrderByScannedAtDesc(String url);

    // Most risky scans globally
    List<ScanHistory> findTop10ByOrderByRiskScoreDesc();

    // Count scans per user
    long countByUser(User user);

    // Count scans by username
    long countByUserUsername(String username);

    // Average risk score for a user
    @Query("""
        SELECT AVG(s.riskScore)
        FROM ScanHistory s
        WHERE s.user.username = :username
    """)
    Double averageRiskScoreByUsername(String username);
}