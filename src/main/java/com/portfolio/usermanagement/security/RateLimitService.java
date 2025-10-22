package com.portfolio.usermanagement.security;

import com.portfolio.usermanagement.entity.RateLimitEntry;
import com.portfolio.usermanagement.repository.RateLimitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Service to handle rate limiting operations with database persistence.
 */
@Service
public class RateLimitService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final int MAX_REQUESTS_PER_HOUR = 1000;
    private static final Duration STALE_ENTRY_THRESHOLD = Duration.ofHours(24);

    private final RateLimitRepository rateLimitRepository;

    public RateLimitService(RateLimitRepository rateLimitRepository) {
        this.rateLimitRepository = rateLimitRepository;
    }

    /**
     * Process a rate limit check and update for a client.
     *
     * @param clientKey the client identifier (IP address)
     * @return RateLimitResult indicating if request is allowed and remaining tokens
     */
    @Transactional
    public RateLimitResult checkRateLimit(String clientKey) {
        // Get or create rate limit entry for this client
        RateLimitEntry entry = rateLimitRepository.findByClientKey(clientKey)
                .orElse(createNewRateLimitEntry(clientKey));

        // Refill tokens based on time elapsed
        refillTokens(entry);

        // Try to consume a token
        boolean allowed = tryConsume(entry);

        if (allowed) {
            // Save updated entry
            entry.setLastAccessTime(Instant.now());
            rateLimitRepository.save(entry);
        }

        return new RateLimitResult(allowed, getRemainingTokens(entry));
    }

    private RateLimitEntry createNewRateLimitEntry(String clientKey) {
        RateLimitEntry entry = new RateLimitEntry();
        entry.setClientKey(clientKey);
        entry.setTokens(MAX_REQUESTS_PER_MINUTE);
        entry.setLastRefillTime(Instant.now());
        entry.setHourlyTokens(MAX_REQUESTS_PER_HOUR);
        entry.setHourlyRefillTime(Instant.now());
        entry.setLastAccessTime(Instant.now());
        return entry;
    }

    private void refillTokens(RateLimitEntry entry) {
        Instant now = Instant.now();

        // Refill per-minute tokens: restore full capacity for each minute passed
        long minutesPassed = Duration.between(entry.getLastRefillTime(), now).toMinutes();
        if (minutesPassed > 0) {
            int newTokens = Math.min(MAX_REQUESTS_PER_MINUTE,
                    entry.getTokens() + (int) minutesPassed * MAX_REQUESTS_PER_MINUTE);
            entry.setTokens(newTokens);
            entry.setLastRefillTime(now);
        }

        // Refill hourly tokens: same logic but on hourly basis
        long hoursPassed = Duration.between(entry.getHourlyRefillTime(), now).toHours();
        if (hoursPassed > 0) {
            int newHourlyTokens = Math.min(MAX_REQUESTS_PER_HOUR,
                    entry.getHourlyTokens() + (int) hoursPassed * MAX_REQUESTS_PER_HOUR);
            entry.setHourlyTokens(newHourlyTokens);
            entry.setHourlyRefillTime(now);
        }
    }

    private boolean tryConsume(RateLimitEntry entry) {
        if (entry.getTokens() > 0 && entry.getHourlyTokens() > 0) {
            entry.setTokens(entry.getTokens() - 1);
            entry.setHourlyTokens(entry.getHourlyTokens() - 1);
            return true;
        }
        return false;
    }

    private int getRemainingTokens(RateLimitEntry entry) {
        return Math.min(entry.getTokens(), entry.getHourlyTokens());
    }

    /**
     * Cleanup stale rate limit entries every hour.
     * Prevents database from growing indefinitely by removing entries
     * that haven't been accessed in 24 hours.
     */
    @Scheduled(fixedRate = 3600000) // Every hour (in milliseconds)
    @Transactional
    public void cleanupStaleEntries() {
        Instant staleThreshold = Instant.now().minus(STALE_ENTRY_THRESHOLD);
        int removed = rateLimitRepository.deleteStaleEntries(staleThreshold);

        if (removed > 0) {
            logger.info("Cleaned up {} stale rate limit entries", removed);
        }
    }

    /**
     * Result class to hold rate limit check results.
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final int remainingTokens;

        public RateLimitResult(boolean allowed, int remainingTokens) {
            this.allowed = allowed;
            this.remainingTokens = remainingTokens;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public int getRemainingTokens() {
            return remainingTokens;
        }
    }
}
