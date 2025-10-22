package com.portfolio.usermanagement.mapper;

import com.portfolio.usermanagement.domain.model.UserDomain;
import com.portfolio.usermanagement.domain.valueobject.Email;
import com.portfolio.usermanagement.domain.valueobject.FullName;
import com.portfolio.usermanagement.domain.valueobject.Username;
import com.portfolio.usermanagement.dto.request.RegisterRequest;
import com.portfolio.usermanagement.dto.response.UserResponse;
import com.portfolio.usermanagement.entity.Role;
import com.portfolio.usermanagement.entity.User;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for User conversions between Entity, Domain, and DTO layers.
 * Uses MapStruct for automatic mapping and custom logic where needed.
 *
 * @author Portfolio Project
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    /**
     * Convert User entity to UserDomain.
     *
     * @param user the User entity
     * @return UserDomain
     */
    @Mapping(target = "username", expression = "java(parseUsername(user.getUsername()))")
    @Mapping(target = "email", expression = "java(parseEmail(user.getEmail()))")
    @Mapping(target = "fullName", expression = "java(parseFullName(user.getFirstName(), user.getLastName()))")
    @Mapping(target = "encryptedPassword", source = "password")
    @Mapping(target = "roleNames", expression = "java(extractRoleNames(user.getRoles()))")
    UserDomain toDomain(User user);

    /**
     * Convert UserDomain to User entity.
     *
     * @param domain the UserDomain
     * @return User entity
     */
    @Mapping(target = "username", expression = "java(domain.getUsername().getValue())")
    @Mapping(target = "email", expression = "java(domain.getEmail().getValue())")
    @Mapping(target = "firstName", expression = "java(domain.getFullName().getFirstName())")
    @Mapping(target = "lastName", expression = "java(domain.getFullName().getLastName())")
    @Mapping(target = "password", source = "encryptedPassword")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "profile", ignore = true)
    User toEntity(UserDomain domain);

    /**
     * Convert UserDomain to UserResponse DTO.
     *
     * @param domain the UserDomain
     * @return UserResponse DTO
     */
    @Mapping(target = "username", expression = "java(domain.getUsername().getValue())")
    @Mapping(target = "email", expression = "java(domain.getEmail().getValue())")
    @Mapping(target = "firstName", expression = "java(domain.getFullName().getFirstName())")
    @Mapping(target = "lastName", expression = "java(domain.getFullName().getLastName())")
    @Mapping(target = "roles", source = "roleNames")
    UserResponse toResponse(UserDomain domain);

    /**
     * Convert User entity directly to UserResponse DTO.
     *
     * @param user the User entity
     * @return UserResponse DTO
     */
    @Mapping(target = "roles", expression = "java(extractRoleNames(user.getRoles()))")
    UserResponse entityToResponse(User user);

    /**
     * Convert RegisterRequest DTO to UserDomain (for new users).
     *
     * @param request the RegisterRequest
     * @param encryptedPassword the encrypted password
     * @return UserDomain
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", expression = "java(parseUsername(request.getUsername()))")
    @Mapping(target = "email", expression = "java(parseEmail(request.getEmail()))")
    @Mapping(target = "fullName", expression = "java(parseFullName(request.getFirstName(), request.getLastName()))")
    @Mapping(target = "encryptedPassword", source = "encryptedPassword")
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "accountNonLocked", constant = "true")
    @Mapping(target = "accountNonExpired", constant = "true")
    @Mapping(target = "credentialsNonExpired", constant = "true")
    @Mapping(target = "roleNames", expression = "java(java.util.Set.of(\"ROLE_USER\"))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserDomain registerRequestToDomain(RegisterRequest request, String encryptedPassword);

    /**
     * Parse username string to Username value object.
     */
    default Username parseUsername(String username) {
        return username != null ? Username.of(username) : null;
    }

    /**
     * Parse email string to Email value object.
     */
    default Email parseEmail(String email) {
        return email != null ? Email.of(email) : null;
    }

    /**
     * Parse first and last name to FullName value object.
     */
    default FullName parseFullName(String firstName, String lastName) {
        return (firstName != null && lastName != null)
            ? FullName.of(firstName, lastName)
            : null;
    }

    /**
     * Extract role names from Role entities.
     */
    default Set<String> extractRoleNames(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
            .map(role -> role.getName().name())
            .collect(Collectors.toSet());
    }

    /**
     * Update existing User entity from UserDomain.
     *
     * @param domain the source UserDomain
     * @param user the target User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", expression = "java(domain.getUsername().getValue())")
    @Mapping(target = "email", expression = "java(domain.getEmail().getValue())")
    @Mapping(target = "firstName", expression = "java(domain.getFullName().getFirstName())")
    @Mapping(target = "lastName", expression = "java(domain.getFullName().getLastName())")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDomain(UserDomain domain, @MappingTarget User user);
}
