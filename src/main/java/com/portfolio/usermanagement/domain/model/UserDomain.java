package com.portfolio.usermanagement.domain.model;

import com.portfolio.usermanagement.domain.valueobject.Email;
import com.portfolio.usermanagement.domain.valueobject.FullName;
import com.portfolio.usermanagement.domain.valueobject.Username;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Domain model for User.
 * Contains business logic and domain rules.
 * This is separate from the JPA entity to maintain clean architecture.
 *
 * @author Portfolio Project
 */
@Getter
@Builder
public class UserDomain {

    private final UUID id;
    private final Username username;
    private final Email email;
    private final FullName fullName;
    private final String encryptedPassword;

    @Builder.Default
    private final Boolean enabled = true;

    @Builder.Default
    private final Boolean accountNonLocked = true;

    @Builder.Default
    private final Boolean accountNonExpired = true;

    @Builder.Default
    private final Boolean credentialsNonExpired = true;

    @Builder.Default
    private final Set<String> roleNames = new HashSet<>();

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * Check if the user has a specific role.
     *
     * @param roleName the role name to check
     * @return true if user has the role
     */
    public boolean hasRole(String roleName) {
        return roleNames.contains(roleName);
    }

    /**
     * Check if the user is an admin.
     *
     * @return true if user has ROLE_ADMIN
     */
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    /**
     * Check if the user is a regular user (not admin).
     *
     * @return true if user has ROLE_USER but not ROLE_ADMIN
     */
    public boolean isRegularUser() {
        return hasRole("ROLE_USER") && !isAdmin();
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

    /**
     * Check if the account can login.
     *
     * @return true if account is active
     */
    public boolean canLogin() {
        return isAccountActive();
    }

    /**
     * Get the number of roles assigned to this user.
     *
     * @return role count
     */
    public int getRoleCount() {
        return roleNames.size();
    }

    /**
     * Check if user has any roles assigned.
     *
     * @return true if user has at least one role
     */
    public boolean hasRoles() {
        return !roleNames.isEmpty();
    }

    /**
     * Check if email belongs to a specific domain.
     *
     * @param domain the domain to check
     * @return true if email is from this domain
     */
    public boolean isEmailFromDomain(String domain) {
        return email.isFromDomain(domain);
    }

    /**
     * Get user's display name for UI.
     * Uses full name if available, otherwise username.
     *
     * @return display name
     */
    public String getDisplayName() {
        if (fullName != null) {
            return fullName.getFullName();
        }
        return username.getValue();
    }

    /**
     * Get user initials for avatar display.
     *
     * @return initials (e.g., "JD" for John Doe)
     */
    public String getInitials() {
        if (fullName != null) {
            return fullName.getInitials();
        }
        String usernameStr = username.getValue();
        return usernameStr.length() >= 2
            ? usernameStr.substring(0, 2).toUpperCase()
            : usernameStr.toUpperCase();
    }

    /**
     * Check if account was recently created (within last 24 hours).
     *
     * @return true if account is new
     */
    public boolean isNewAccount() {
        if (createdAt == null) {
            return false;
        }
        return createdAt.isAfter(LocalDateTime.now().minusDays(1));
    }

    /**
     * Check if account has been modified recently (within last hour).
     *
     * @return true if recently modified
     */
    public boolean isRecentlyModified() {
        if (updatedAt == null) {
            return false;
        }
        return updatedAt.isAfter(LocalDateTime.now().minusHours(1));
    }

    /**
     * Validate if user can perform administrative actions.
     *
     * @throws IllegalStateException if user is not an admin
     */
    public void requireAdmin() {
        if (!isAdmin()) {
            throw new IllegalStateException("User does not have admin privileges");
        }
    }

    /**
     * Validate if account is active.
     *
     * @throws IllegalStateException if account is not active
     */
    public void requireActiveAccount() {
        if (!isAccountActive()) {
            throw new IllegalStateException("User account is not active");
        }
    }
}
