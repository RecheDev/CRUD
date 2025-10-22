package com.portfolio.usermanagement.repository;

import com.portfolio.usermanagement.entity.RefreshToken;
import com.portfolio.usermanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RefreshToken entity.
 * Provides methods to manage refresh tokens for JWT authentication.
 *
 * @author Portfolio Project
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find a refresh token by its token string.
     *
     * @param token the refresh token string
     * @return Optional containing the RefreshToken if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all refresh tokens for a specific user.
     *
     * @param user the user
     * @return list of refresh tokens
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Find all valid (not revoked, not expired) tokens for a user.
     *
     * @param user the user
     * @param now current timestamp
     * @return list of valid refresh tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user " +
           "AND rt.revoked = false AND rt.expiryDate > :now")
    List<RefreshToken> findValidTokensByUser(@Param("user") User user, @Param("now") Instant now);

    /**
     * Delete all refresh tokens for a specific user.
     * Used when user is deleted or all sessions need to be terminated.
     *
     * @param user the user
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteByUser(@Param("user") User user);

    /**
     * Delete all expired refresh tokens.
     * Should be called periodically to clean up the database.
     *
     * @param now current timestamp
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    int deleteExpiredTokens(@Param("now") Instant now);

    /**
     * Delete all revoked tokens older than a certain date.
     * Used for cleanup of already-revoked tokens.
     *
     * @param date cutoff date
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true AND rt.revokedAt < :date")
    int deleteRevokedTokensOlderThan(@Param("date") Instant date);

    /**
     * Check if a refresh token exists and is valid.
     *
     * @param token the refresh token string
     * @param now current timestamp
     * @return true if token exists, is not revoked, and not expired
     */
    @Query("SELECT CASE WHEN COUNT(rt) > 0 THEN true ELSE false END FROM RefreshToken rt " +
           "WHERE rt.token = :token AND rt.revoked = false AND rt.expiryDate > :now")
    boolean existsValidToken(@Param("token") String token, @Param("now") Instant now);

    /**
     * Count active (valid) tokens for a user.
     *
     * @param user the user
     * @param now current timestamp
     * @return count of valid tokens
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user " +
           "AND rt.revoked = false AND rt.expiryDate > :now")
    long countValidTokensByUser(@Param("user") User user, @Param("now") Instant now);
}
