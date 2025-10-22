package com.portfolio.usermanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entity representing a refresh token for JWT token rotation.
 * Refresh tokens are long-lived tokens used to obtain new access tokens.
 *
 * @author Portfolio Project
 */
@Entity
@Table(
    name = "refresh_tokens",
    indexes = {
        @Index(name = "idx_refresh_token", columnList = "token", unique = true),
        @Index(name = "idx_refresh_user_id", columnList = "user_id"),
        @Index(name = "idx_refresh_expiry", columnList = "expiry_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * The refresh token string (UUID-based).
     */
    @Column(name = "token", nullable = false, unique = true, length = 36)
    private String token;

    /**
     * The user who owns this refresh token.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * When this refresh token expires.
     */
    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    /**
     * IP address from which the token was created (security audit).
     */
    @Column(name = "created_from_ip", length = 45)
    private String createdFromIp;

    /**
     * Whether this token has been revoked before expiration.
     */
    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    /**
     * When this token was revoked (if applicable).
     */
    @Column(name = "revoked_at")
    private Instant revokedAt;

    /**
     * Check if this refresh token is expired.
     *
     * @return true if current time is after expiry date
     */
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }

    /**
     * Check if this refresh token is valid (not expired and not revoked).
     *
     * @return true if token is valid
     */
    public boolean isValid() {
        return !isExpired() && !Boolean.TRUE.equals(revoked);
    }

    /**
     * Revoke this refresh token.
     */
    public void revoke() {
        this.revoked = true;
        this.revokedAt = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.revoked == null) {
            this.revoked = false;
        }
    }
}
