package com.portfolio.usermanagement.repository;

import com.portfolio.usermanagement.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository for managing blacklisted JWT tokens.
 */
@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    /**
     * Find a blacklisted token by its JTI (JWT ID).
     *
     * @param jti the JWT ID
     * @return Optional containing the blacklisted token if found
     */
    Optional<BlacklistedToken> findByJti(String jti);

    /**
     * Check if a token is blacklisted by its JTI.
     *
     * @param jti the JWT ID
     * @return true if the token exists in the blacklist
     */
    boolean existsByJti(String jti);

    /**
     * Delete all expired tokens (tokens whose expiry time has passed).
     *
     * @param now current timestamp
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM BlacklistedToken bt WHERE bt.expiryTime < :now")
    int deleteExpiredTokens(@Param("now") Instant now);

    /**
     * Count how many tokens are currently blacklisted.
     *
     * @return count of blacklisted tokens
     */
    @Query("SELECT COUNT(bt) FROM BlacklistedToken bt")
    long countBlacklistedTokens();
}
