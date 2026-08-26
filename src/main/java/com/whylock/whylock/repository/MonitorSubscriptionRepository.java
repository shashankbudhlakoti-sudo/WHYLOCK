package com.whylock.whylock.repository;

import com.whylock.whylock.model.MonitorSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonitorSubscriptionRepository extends JpaRepository<MonitorSubscription, String> {
    List<MonitorSubscription> findByUserId(Long userId);
    List<MonitorSubscription> findByUrl(String url);
    List<MonitorSubscription> findByIsActiveTrueAndCheckIntervalMinutes(int checkInterval);
    Optional<MonitorSubscription> findByUserIdAndUrl(Long userId, String url);
    List<MonitorSubscription> findByIsActiveTrue();
    List<MonitorSubscription> findByEmailAndIsActiveTrue(String email);
    Optional<MonitorSubscription> findByUrlAndEmail(String url, String email);
    Optional<MonitorSubscription> findByUnsubscribeToken(String token);
}
