package com.whylock.whylock.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    // One bucket per user — stored in memory
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Try to consume 1 token from user's bucket.
     * Returns true if allowed, false if rate limited.
     */
    public boolean tryConsume(String username) {
        Bucket bucket = buckets.computeIfAbsent(username, this::createBucket);
        return bucket.tryConsume(1);
    }

    /**
     * Get remaining tokens for a user.
     */
    public long getAvailableTokens(String username) {
        Bucket bucket = buckets.computeIfAbsent(username, this::createBucket);
        return bucket.getAvailableTokens();
    }

    /**
     * Create a new bucket for a user:
     * - 5 requests per minute (short burst protection)
     * - 50 requests per hour (daily quota)
     */
    private Bucket createBucket(String username) {
        // 5 scans per minute
        Bandwidth minuteLimit = Bandwidth.classic(
                5, Refill.greedy(5, Duration.ofMinutes(1))
        );

        // 50 scans per hour
        Bandwidth hourlyLimit = Bandwidth.classic(
                50, Refill.greedy(50, Duration.ofHours(1))
        );

        return Bucket.builder()
                .addLimit(minuteLimit)
                .addLimit(hourlyLimit)
                .build();
    }
}