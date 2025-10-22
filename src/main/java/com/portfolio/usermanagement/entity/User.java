package com.portfolio.usermanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User entity representing a system user.
 * Core entity of the application with authentication and authorization information.
 *
 * Features:
 * - UUID-based primary key for better security and distributed systems
 * - Many-to-Many relationship with Role for RBAC
 * - One-to-One relationship with Profile for extended information
 * - Account status tracking (enabled/locked)
 * - Audit fields inherited from BaseEntity
 *
 * @author Portfolio Project
 */
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_user_username", columnList = "username", unique = true),
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_enabled", columnList = "enabled")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password", "roles", "profile"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "account_non_locked", nullable = false)
    @Builder.Default
    private Boolean accountNonLocked = true;

    @Column(name = "account_non_expired", nullable = false)
    @Builder.Default
    private Boolean accountNonExpired = true;

    @Column(name = "credentials_non_expired", nullable = false)
    @Builder.Default
    private Boolean credentialsNonExpired = true;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
        indexes = {
            @Index(name = "idx_user_roles_user", columnList = "user_id"),
            @Index(name = "idx_user_roles_role", columnList = "role_id")
        }
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @PrimaryKeyJoinColumn
    private Profile profile;

    /**
     * Helper method to add a role to this user.
     * Maintains bidirectional relationship consistency.
     *
     * @param role the role to add
     */
    public void addRole(Role role) {
        roles.add(role);
        role.getUsers().add(this);
    }

    /**
     * Helper method to remove a role from this user.
     * Maintains bidirectional relationship consistency.
     *
     * @param role the role to remove
     */
    public void removeRole(Role role) {
        roles.remove(role);
        role.getUsers().remove(this);
    }

    /**
     * Helper method to set the user profile.
     * Maintains bidirectional relationship consistency.
     *
     * @param profile the profile to set
     */
    public void setProfile(Profile profile) {
        if (profile == null) {
            if (this.profile != null) {
                this.profile.setUser(null);
            }
        } else {
            profile.setUser(this);
        }
        this.profile = profile;
    }

    /**
     * Get the user's full name.
     *
     * @return concatenated first and last name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if the user has a specific role.
     *
     * @param roleName the role name to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(Role.RoleName roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    /**
     * Check if the user is an admin.
     *
     * @return true if user has ROLE_ADMIN
     */
    public boolean isAdmin() {
        return hasRole(Role.RoleName.ROLE_ADMIN);
    }

    /**
     * Check if the user account is fully active and usable.
     *
     * @return true if account is enabled, not locked, and not expired
     */
    public boolean isAccountActive() {
        return Boolean.TRUE.equals(enabled)
            && Boolean.TRUE.equals(accountNonLocked)
            && Boolean.TRUE.equals(accountNonExpired)
            && Boolean.TRUE.equals(credentialsNonExpired);
    }
}
