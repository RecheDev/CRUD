package com.portfolio.usermanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entity to persist failed login attempts in the database.
 * Tracks attempts and lockout status for account security.
 */
@Entity
@Table(name = "login_attempts",
       indexes = {
           @Index(name = "idx_username", columnList = "username"),
           @Index(name = "idx_lock_until", columnList = "lock_until")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Username attempting to login
     */
    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    /**
     * Number of failed login attempts
     */
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    /**
     * Timestamp of the first failed attempt in the current window
     */
    @Column(name = "first_attempt_time", nullable = false)
    private Instant firstAttemptTime;

    /**
     * Timestamp until which the account is locked (null if not locked)
     */
    @Column(name = "lock_until")
    private Instant lockUntil;

    /**
     * Last time this record was updated
     */
    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = Instant.now();
    }
}
