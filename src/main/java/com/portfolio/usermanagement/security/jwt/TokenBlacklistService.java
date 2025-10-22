package com.portfolio.usermanagement.security.jwt;

import com.portfolio.usermanagement.entity.BlacklistedToken;
import com.portfolio.usermanagement.repository.BlacklistedTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;

/**
 * Service to manage JWT token blacklisting for logout functionality.
 * Tokens are blacklisted when users logout and automatically cleaned up after expiration.
 *
 * Now uses database persistence instead of in-memory storage for better reliability
 * and persistence across application restarts.
 */
@Service
public class TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public TokenBlacklistService(BlacklistedTokenRepository blacklistedTokenRepository) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    /**
     * Blacklist a token by its JTI.
     *
     * @param jti    the JWT ID (unique token identifier)
     * @param expiry the token expiration date
     */
    @Transactional
    public void blacklistToken(String jti, Date expiry) {
        blacklistToken(jti, expiry, null);
    }

    /**
     * Blacklist a token by its JTI with username.
     *
     * @param jti      the JWT ID (unique token identifier)
     * @param expiry   the token expiration date
     * @param username the username associated with the token (optional)
     */
    @Transactional
    public void blacklistToken(String jti, Date expiry, String username) {
        if (jti == null || expiry == null) {
            logger.warn("Attempted to blacklist token with null JTI or expiry");
            return;
        }

        // Check if already blacklisted to avoid duplicates
        if (blacklistedTokenRepository.existsByJti(jti)) {
            logger.debug("Token already blacklisted: {}", jti);
            return;
        }

        BlacklistedToken token = new BlacklistedToken();
        token.setJti(jti);
        token.setExpiryTime(expiry.toInstant());
        token.setBlacklistedAt(Instant.now());
        token.setUsername(username);

        blacklistedTokenRepository.save(token);
        logger.debug("Token blacklisted: {}", jti);
    }

    /**
     * Check if a token is blacklisted.
     *
     * @param jti the JWT ID to check
     * @return true if the token is blacklisted, false otherwise
     */
    public boolean isBlacklisted(String jti) {
        if (jti == null) {
            return false;
        }
        return blacklistedTokenRepository.existsByJti(jti);
    }

    /**
     * Clean up expired tokens from the blacklist.
     * Runs every hour to prevent database from growing indefinitely.
     * Expired tokens are safe to remove since they cannot be used anyway.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    @Transactional
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        long initialSize = blacklistedTokenRepository.countBlacklistedTokens();

        int removed = blacklistedTokenRepository.deleteExpiredTokens(now);

        if (removed > 0) {
            logger.info("Cleaned up {} expired tokens from blacklist (total before: {})", removed, initialSize);
        }
    }

    /**
     * Get the current size of the blacklist.
     *
     * @return number of blacklisted tokens
     */
    public long getBlacklistSize() {
        return blacklistedTokenRepository.countBlacklistedTokens();
    }

    /**
     * Clear all blacklisted tokens (for testing purposes).
     */
    @Transactional
    public void clearAll() {
        blacklistedTokenRepository.deleteAll();
        logger.warn("Token blacklist cleared");
    }
}
