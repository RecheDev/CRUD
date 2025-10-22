package com.portfolio.usermanagement.security;

import com.portfolio.usermanagement.entity.LoginAttempt;
import com.portfolio.usermanagement.repository.LoginAttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Service to track failed login attempts and lock accounts after threshold is exceeded.
 * Implements automatic unlocking after a configured timeout period.
 *
 * Now uses database persistence instead of in-memory storage for better reliability
 * and persistence across application restarts.
 */
@Service
public class AccountLockoutService {

    private static final Logger logger = LoggerFactory.getLogger(AccountLockoutService.class);

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(30);
    private static final Duration FAILED_ATTEMPTS_WINDOW = Duration.ofMinutes(15);

    private final LoginAttemptRepository loginAttemptRepository;

    public AccountLockoutService(LoginAttemptRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

    /**
     * Record a successful login and clear any failed attempts.
     */
    @Transactional
    public void loginSucceeded(String username) {
        loginAttemptRepository.deleteByUsername(username);
        logger.debug("Login succeeded for user: {}", username);
    }

    /**
     * Record a failed login attempt and lock account if threshold exceeded.
     */
    @Transactional
    public void loginFailed(String username) {
        Instant now = Instant.now();

        // Find existing attempt record or create new one
        LoginAttempt attempt = loginAttemptRepository.findByUsername(username)
                .orElse(createNewLoginAttempt(username, now));

        // Reset attempts if the time window has expired (15 minutes)
        // This means attempts older than 15 minutes don't count toward lockout
        if (Duration.between(attempt.getFirstAttemptTime(), now).compareTo(FAILED_ATTEMPTS_WINDOW) > 0) {
            attempt.setAttemptCount(0);
            attempt.setFirstAttemptTime(now);
            attempt.setLockUntil(null);
        }

        // Increment attempt count
        int currentAttempts = attempt.getAttemptCount() + 1;
        attempt.setAttemptCount(currentAttempts);
        attempt.setLastUpdated(now);

        // Lock account if max attempts reached within the time window
        if (currentAttempts >= MAX_FAILED_ATTEMPTS) {
            attempt.setLockUntil(now.plus(LOCKOUT_DURATION));
            logger.warn("Account locked due to {} failed login attempts: {}", MAX_FAILED_ATTEMPTS, username);
        } else {
            logger.debug("Failed login attempt {} for user: {}", currentAttempts, username);
        }

        loginAttemptRepository.save(attempt);
    }

    /**
     * Check if an account is currently locked.
     */
    public boolean isLocked(String username) {
        Optional<LoginAttempt> attemptOpt = loginAttemptRepository.findByUsername(username);
        if (attemptOpt.isEmpty()) {
            return false;
        }

        LoginAttempt attempt = attemptOpt.get();
        Instant now = Instant.now();
        Instant lockUntil = attempt.getLockUntil();

        // Check if lock has expired
        if (lockUntil != null && now.isAfter(lockUntil)) {
            loginAttemptRepository.deleteByUsername(username);
            logger.info("Account lock expired for user: {}", username);
            return false;
        }

        return lockUntil != null && now.isBefore(lockUntil);
    }

    /**
     * Get the number of failed login attempts for a user.
     */
    public int getFailedAttempts(String username) {
        return loginAttemptRepository.findByUsername(username)
                .map(LoginAttempt::getAttemptCount)
                .orElse(0);
    }

    /**
     * Get the time remaining until account is unlocked (null if not locked).
     */
    public Duration getRemainingLockoutTime(String username) {
        Optional<LoginAttempt> attemptOpt = loginAttemptRepository.findByUsername(username);
        if (attemptOpt.isEmpty()) {
            return null;
        }

        Instant lockUntil = attemptOpt.get().getLockUntil();
        if (lockUntil == null) {
            return null;
        }

        Instant now = Instant.now();
        if (now.isAfter(lockUntil)) {
            return null;
        }

        return Duration.between(now, lockUntil);
    }

    /**
     * Manually unlock an account (e.g., by admin action).
     */
    @Transactional
    public void unlock(String username) {
        loginAttemptRepository.deleteByUsername(username);
        logger.info("Account manually unlocked: {}", username);
    }

    /**
     * Cleanup expired login attempt records every hour.
     * Prevents database from growing indefinitely by removing stale data.
     */
    @Scheduled(fixedRate = 3600000) // Every hour (in milliseconds)
    @Transactional
    public void cleanupExpiredAttempts() {
        Instant now = Instant.now();
        Instant lockExpiredBefore = now;
        Instant attemptExpiredBefore = now.minus(FAILED_ATTEMPTS_WINDOW);

        int removed = loginAttemptRepository.deleteExpiredAttempts(lockExpiredBefore, attemptExpiredBefore);

        if (removed > 0) {
            logger.info("Cleaned up {} expired login attempt records", removed);
        }
    }

    /**
     * Helper method to create a new LoginAttempt entity.
     */
    private LoginAttempt createNewLoginAttempt(String username, Instant now) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUsername(username);
        attempt.setAttemptCount(0);
        attempt.setFirstAttemptTime(now);
        attempt.setLastUpdated(now);
        return attempt;
    }
}
