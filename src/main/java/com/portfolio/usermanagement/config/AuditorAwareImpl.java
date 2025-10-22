package com.portfolio.usermanagement.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementation of AuditorAware to provide the current auditor (user) for JPA auditing.
 *
 * This class is used by Spring Data JPA's auditing feature to automatically populate
 * the @CreatedBy and @LastModifiedBy fields in entities.
 *
 * The current user is retrieved from Spring Security's SecurityContext.
 *
 * @author Portfolio Project
 */
@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<String> {

    /**
     * Returns the current auditor (username) from the security context.
     *
     * If no user is authenticated, returns "system" as the default auditor.
     * This is useful for system-generated operations or initialization scripts.
     *
     * @return the username of the current authenticated user, or "system" if not authenticated
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("system");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return Optional.of(userDetails.getUsername());
        }

        if (principal instanceof String username) {
            return Optional.of(username);
        }

        return Optional.of("system");
    }
}
