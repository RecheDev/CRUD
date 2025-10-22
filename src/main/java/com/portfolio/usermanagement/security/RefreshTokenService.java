package com.portfolio.usermanagement.security;

import com.portfolio.usermanagement.entity.RefreshToken;
import com.portfolio.usermanagement.entity.User;
import com.portfolio.usermanagement.exception.UnauthorizedException;
import com.portfolio.usermanagement.exception.ErrorCode;
import com.portfolio.usermanagement.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing refresh tokens.
 * Handles creation, validation, rotation, and cleanup of refresh tokens.
 *
 * @author Portfolio Project
 */
@Service
@Transactional
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    /**
     * Refresh token expiration in milliseconds.
     * Default: 7 days (604,800,000 ms)
     */
    @Value("${jwt.refresh.expiration:604800000}")
    private Long refreshTokenDurationMs;

    /**
     * Maximum number of active refresh tokens per user.
     * Prevents token accumulation from multiple devices.
     */
    @Value("${jwt.refresh.max-tokens-per-user:5}")
    private int maxTokensPerUser;

    /**
     * Create a new refresh token for a user.
     *
     * @param user the user
     * @param ipAddress the IP address from which the token is created
     * @return the created RefreshToken
     */
    public RefreshToken createRefreshToken(User user, String ipAddress) {
        // Clean up old tokens if user has too many
        cleanupExcessTokensForUser(user);

        // Generate unique token
        String token = UUID.randomUUID().toString();

        // Create refresh token entity
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plus(Duration.ofMillis(refreshTokenDurationMs)))
                .createdFromIp(ipAddress)
                .revoked(false)
                .build();

        refreshToken = refreshTokenRepository.save(refreshToken);
        logger.debug("Created refresh token for user: {}", user.getUsername());

        return refreshToken;
    }

    /**
     * Verify and retrieve a refresh token.
     * Throws exception if token is invalid, expired, or revoked.
     *
     * @param token the refresh token string
     * @return the RefreshToken entity
     * @throws UnauthorizedException if token is invalid
     */
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException(
                        "Invalid refresh token",
                        ErrorCode.INVALID_TOKEN
                ));

        if (refreshToken.isExpired()) {
            // Delete expired token
            refreshTokenRepository.delete(refreshToken);
            logger.warn("Refresh token expired for user: {}", refreshToken.getUser().getUsername());
            throw new UnauthorizedException(
                    "Refresh token has expired. Please log in again.",
                    ErrorCode.TOKEN_EXPIRED
            );
        }

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            logger.warn("Attempted to use revoked refresh token for user: {}",
                    refreshToken.getUser().getUsername());
            throw new UnauthorizedException(
                    "Refresh token has been revoked",
                    ErrorCode.INVALID_TOKEN
            );
        }

        return refreshToken;
    }

    /**
     * Rotate a refresh token (revoke old, create new).
     * This is the recommended approach for security.
     *
     * @param oldToken the old refresh token string
     * @param ipAddress the IP address for the new token
     * @return the new RefreshToken
     */
    public RefreshToken rotateRefreshToken(String oldToken, String ipAddress) {
        // Verify old token
        RefreshToken oldRefreshToken = verifyRefreshToken(oldToken);

        // Revoke old token
        oldRefreshToken.revoke();
        refreshTokenRepository.save(oldRefreshToken);

        // Create new token
        RefreshToken newRefreshToken = createRefreshToken(oldRefreshToken.getUser(), ipAddress);

        logger.info("Rotated refresh token for user: {}", oldRefreshToken.getUser().getUsername());

        return newRefreshToken;
    }

    /**
     * Revoke a specific refresh token.
     *
     * @param token the refresh token string
     */
    public void revokeRefreshToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
        if (refreshTokenOpt.isPresent()) {
            RefreshToken refreshToken = refreshTokenOpt.get();
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
            logger.info("Revoked refresh token for user: {}", refreshToken.getUser().getUsername());
        }
    }

    /**
     * Revoke all refresh tokens for a user.
     * Used when user logs out from all devices or changes password.
     *
     * @param user the user
     */
    public void revokeAllUserTokens(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUser(user);
        tokens.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(tokens);
        logger.info("Revoked all refresh tokens for user: {}", user.getUsername());
    }

    /**
     * Delete all refresh tokens for a user.
     * Used when user account is deleted.
     *
     * @param user the user
     */
    public void deleteAllUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
        logger.info("Deleted all refresh tokens for user: {}", user.getUsername());
    }

    /**
     * Clean up excess tokens for a user to prevent accumulation.
     * Keeps only the most recent tokens up to maxTokensPerUser.
     *
     * @param user the user
     */
    private void cleanupExcessTokensForUser(User user) {
        long activeTokenCount = refreshTokenRepository.countValidTokensByUser(user, Instant.now());

        if (activeTokenCount >= maxTokensPerUser) {
            // Get all valid tokens, sorted by creation date (oldest first)
            List<RefreshToken> validTokens = refreshTokenRepository.findValidTokensByUser(user, Instant.now());

            // Calculate how many to remove
            int tokensToRemove = (int) (activeTokenCount - maxTokensPerUser + 1);

            // Revoke oldest tokens
            validTokens.stream()
                    .limit(tokensToRemove)
                    .forEach(token -> {
                        token.revoke();
                        refreshTokenRepository.save(token);
                    });

            logger.info("Cleaned up {} excess tokens for user: {}", tokensToRemove, user.getUsername());
        }
    }

    /**
     * Scheduled task to clean up expired tokens.
     * Runs every hour.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanupExpiredTokens() {
        int deletedCount = refreshTokenRepository.deleteExpiredTokens(Instant.now());
        if (deletedCount > 0) {
            logger.info("Cleaned up {} expired refresh tokens", deletedCount);
        }
    }

    /**
     * Scheduled task to clean up old revoked tokens.
     * Runs daily, removes tokens revoked more than 30 days ago.
     */
    @Scheduled(fixedRate = 86400000) // 24 hours
    public void cleanupOldRevokedTokens() {
        Instant cutoffDate = Instant.now().minus(Duration.ofDays(30));
        int deletedCount = refreshTokenRepository.deleteRevokedTokensOlderThan(cutoffDate);
        if (deletedCount > 0) {
            logger.info("Cleaned up {} old revoked refresh tokens", deletedCount);
        }
    }

    /**
     * Get count of active tokens for a user.
     *
     * @param user the user
     * @return count of valid tokens
     */
    public long getActiveTokenCount(User user) {
        return refreshTokenRepository.countValidTokensByUser(user, Instant.now());
    }

    /**
     * Check if a token is valid without throwing exceptions.
     *
     * @param token the refresh token string
     * @return true if token is valid
     */
    public boolean isTokenValid(String token) {
        return refreshTokenRepository.existsValidToken(token, Instant.now());
    }
}
