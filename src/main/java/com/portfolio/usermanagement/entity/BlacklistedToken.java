package com.portfolio.usermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entity to persist blacklisted JWT tokens in the database.
 * Replaces in-memory storage for better persistence across restarts.
 */
@Entity
@Table(name = "blacklisted_tokens",
       indexes = {
           @Index(name = "idx_jti", columnList = "jti"),
           @Index(name = "idx_expiry", columnList = "expiry_time")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * JWT ID (jti claim) - unique identifier for the token
     */
    @Column(name = "jti", nullable = false, unique = true, length = 255)
    private String jti;

    /**
     * When the token expires (after this time, it can be safely removed)
     */
    @Column(name = "expiry_time", nullable = false)
    private Instant expiryTime;

    /**
     * When the token was blacklisted
     */
    @Column(name = "blacklisted_at", nullable = false)
    private Instant blacklistedAt;

    /**
     * Username associated with the token (for auditing)
     */
    @Column(name = "username", length = 100)
    private String username;
}
