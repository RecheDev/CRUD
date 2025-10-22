package com.portfolio.usermanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Role entity representing a user role in the system.
 * Roles are used for role-based access control (RBAC).
 *
 * Common roles include:
 * - ROLE_USER: Standard user with basic permissions
 * - ROLE_ADMIN: Administrator with full system access
 * - ROLE_MODERATOR: Moderator with elevated permissions
 *
 * @author Portfolio Project
 */
@Entity
@Table(
    name = "roles",
    indexes = {
        @Index(name = "idx_role_name", columnList = "name", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "users")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    @Enumerated(EnumType.STRING)
    private RoleName name;

    @Column(name = "description", length = 200)
    private String description;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> users = new HashSet<>();

    /**
     * Enum representing available role names in the system.
     */
    public enum RoleName {
        ROLE_USER,
        ROLE_ADMIN,
        ROLE_MODERATOR
    }

    /**
     * Constructor with name only.
     *
     * @param name the role name
     */
    public Role(RoleName name) {
        this.name = name;
    }

    /**
     * Constructor with name and description.
     *
     * @param name        the role name
     * @param description the role description
     */
    public Role(RoleName name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Helper method to add a user to this role.
     *
     * @param user the user to add
     */
    public void addUser(User user) {
        users.add(user);
        user.getRoles().add(this);
    }

    /**
     * Helper method to remove a user from this role.
     *
     * @param user the user to remove
     */
    public void removeUser(User user) {
        users.remove(user);
        user.getRoles().remove(this);
    }
}
