package com.portfolio.usermanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Profile entity representing additional user information.
 * Each user has exactly one profile (One-to-One relationship).
 *
 * Contains optional extended information about the user such as:
 * - Biography
 * - Avatar URL
 * - Contact information
 * - Personal details
 *
 * @author Portfolio Project
 */
@Entity
@Table(
    name = "profiles",
    indexes = {
        @Index(name = "idx_profile_user_id", columnList = "user_id", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Profile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @MapsId
    private User user;

    /**
     * Constructor with required user relationship.
     *
     * @param user the user this profile belongs to
     */
    public Profile(User user) {
        this.user = user;
    }
}
