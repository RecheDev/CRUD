package com.portfolio.usermanagement.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Domain model for Role.
 * Contains business logic and domain rules for roles.
 *
 * @author Portfolio Project
 */
@Getter
@Builder
public class RoleDomain {

    public enum RoleName {
        ROLE_USER,
        ROLE_ADMIN,
        ROLE_MODERATOR
    }

    private final Long id;
    private final RoleName name;
    private final String description;

    /**
     * Check if this is the admin role.
     *
     * @return true if ROLE_ADMIN
     */
    public boolean isAdmin() {
        return RoleName.ROLE_ADMIN.equals(name);
    }

    /**
     * Check if this is the user role.
     *
     * @return true if ROLE_USER
     */
    public boolean isUser() {
        return RoleName.ROLE_USER.equals(name);
    }

    /**
     * Check if this is the moderator role.
     *
     * @return true if ROLE_MODERATOR
     */
    public boolean isModerator() {
        return RoleName.ROLE_MODERATOR.equals(name);
    }

    /**
     * Get the role name as a string.
     *
     * @return role name string
     */
    public String getRoleNameString() {
        return name.name();
    }

    /**
     * Check if this role has higher privileges than another role.
     * Hierarchy: ADMIN > MODERATOR > USER
     *
     * @param other the other role
     * @return true if this role has higher privileges
     */
    public boolean hasHigherPrivilegesThan(RoleDomain other) {
        return getPrivilegeLevel() > other.getPrivilegeLevel();
    }

    /**
     * Get the privilege level of this role.
     *
     * @return privilege level (higher is more privileged)
     */
    private int getPrivilegeLevel() {
        return switch (name) {
            case ROLE_ADMIN -> 3;
            case ROLE_MODERATOR -> 2;
            case ROLE_USER -> 1;
        };
    }
}
