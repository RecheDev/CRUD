package com.portfolio.usermanagement.repository;

import com.portfolio.usermanagement.entity.RateLimitEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository for managing rate limit entries.
 */
@Repository
public interface RateLimitRepository extends JpaRepository<RateLimitEntry, Long> {

    /**
     * Find rate limit entry by client key (IP address).
     *
     * @param clientKey the client identifier
     * @return Optional containing the rate limit entry if found
     */
    Optional<RateLimitEntry> findByClientKey(String clientKey);

    /**
     * Delete stale rate limit entries that haven't been accessed recently.
     * This prevents the table from growing indefinitely.
     *
     * @param lastAccessBefore timestamp before which entries are considered stale
     * @return number of deleted entries
     */
    @Modifying
    @Query("DELETE FROM RateLimitEntry rle WHERE rle.lastAccessTime < :lastAccessBefore")
    int deleteStaleEntries(@Param("lastAccessBefore") Instant lastAccessBefore);

    /**
     * Count active rate limit entries.
     *
     * @return count of entries
     */
    @Query("SELECT COUNT(rle) FROM RateLimitEntry rle")
    long countEntries();
}
