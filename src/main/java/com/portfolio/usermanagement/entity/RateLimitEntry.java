package com.portfolio.usermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entity to persist rate limiting data in the database.
 * Tracks API request rates per client IP address.
 */
@Entity
@Table(name = "rate_limit_entries",
       indexes = {
           @Index(name = "idx_client_key", columnList = "client_key"),
           @Index(name = "idx_last_refill", columnList = "last_refill_time")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Client identifier (typically IP address)
     */
    @Column(name = "client_key", nullable = false, unique = true, length = 100)
    private String clientKey;

    /**
     * Number of available tokens for per-minute rate limit
     */
    @Column(name = "tokens", nullable = false)
    private Integer tokens;

    /**
     * Last time the per-minute tokens were refilled
     */
    @Column(name = "last_refill_time", nullable = false)
    private Instant lastRefillTime;

    /**
     * Number of available tokens for per-hour rate limit
     */
    @Column(name = "hourly_tokens", nullable = false)
    private Integer hourlyTokens;

    /**
     * Last time the hourly tokens were refilled
     */
    @Column(name = "hourly_refill_time", nullable = false)
    private Instant hourlyRefillTime;

    /**
     * Last time this entry was accessed/updated
     */
    @Column(name = "last_access_time", nullable = false)
    private Instant lastAccessTime;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastAccessTime = Instant.now();
    }
}
