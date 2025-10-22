package com.portfolio.usermanagement.repository;

import com.portfolio.usermanagement.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository for managing login attempts and account lockouts.
 */
@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    /**
     * Find login attempt record by username.
     *
     * @param username the username
     * @return Optional containing the login attempt if found
     */
    Optional<LoginAttempt> findByUsername(String username);

    /**
     * Delete login attempt record for a specific username.
     *
     * @param username the username
     */
    void deleteByUsername(String username);

    /**
     * Delete all expired login attempt records.
     * A record is expired if:
     * - The lock period has expired, OR
     * - The tracking window has expired
     *
     * @param lockExpiredBefore timestamp for lock expiration check
     * @param attemptExpiredBefore timestamp for attempt window expiration check
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM LoginAttempt la WHERE " +
           "(la.lockUntil IS NOT NULL AND la.lockUntil < :lockExpiredBefore) OR " +
           "(la.lockUntil IS NULL AND la.firstAttemptTime < :attemptExpiredBefore)")
    int deleteExpiredAttempts(
        @Param("lockExpiredBefore") Instant lockExpiredBefore,
        @Param("attemptExpiredBefore") Instant attemptExpiredBefore
    );
}
