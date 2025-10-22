package com.portfolio.usermanagement.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;

/**
 * Aspect for auditing security-sensitive operations.
 * Logs authentication, authorization, and data modification events.
 */
@Aspect
@Component
public class AuditLoggingAspect {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    /**
     * Log successful authentication attempts (login).
     * Triggered after the login method completes successfully.
     */
    @AfterReturning(
        pointcut = "execution(* com.portfolio.usermanagement.service.impl.AuthServiceImpl.login(..))",
        returning = "result"
    )
    public void logSuccessfulLogin(JoinPoint joinPoint, Object result) {
        String username = extractUsername(joinPoint.getArgs());
        auditLogger.info("LOGIN_SUCCESS | user={} | timestamp={} | ip={}",
            username,
            Instant.now(),
            getClientInfo()
        );
    }

    @AfterThrowing(
        pointcut = "execution(* com.portfolio.usermanagement.service.impl.AuthServiceImpl.login(..))",
        throwing = "exception"
    )
    public void logFailedLogin(JoinPoint joinPoint, Exception exception) {
        String username = extractUsername(joinPoint.getArgs());
        auditLogger.warn("LOGIN_FAILURE | user={} | timestamp={} | ip={} | reason={}",
            username,
            Instant.now(),
            getClientInfo(),
            exception.getClass().getSimpleName()
        );
    }

    /**
     * Log user registration.
     */
    @AfterReturning(
        pointcut = "execution(* com.portfolio.usermanagement.service.impl.AuthServiceImpl.register(..))",
        returning = "result"
    )
    public void logUserRegistration(JoinPoint joinPoint, Object result) {
        String username = extractUsername(joinPoint.getArgs());
        auditLogger.info("USER_REGISTRATION | user={} | timestamp={} | ip={}",
            username,
            Instant.now(),
            getClientInfo()
        );
    }

    /**
     * Log user updates.
     */
    @AfterReturning(
        pointcut = "execution(* com.portfolio.usermanagement.service.UserService.updateUser(..))",
        returning = "result"
    )
    public void logUserUpdate(JoinPoint joinPoint, Object result) {
        String actor = getCurrentUsername();
        Object[] args = joinPoint.getArgs();
        String targetUserId = args.length > 0 ? args[0].toString() : "unknown";

        auditLogger.info("USER_UPDATE | actor={} | targetUserId={} | timestamp={} | ip={}",
            actor,
            targetUserId,
            Instant.now(),
            getClientInfo()
        );
    }

    /**
     * Log user deletion.
     */
    @AfterReturning(
        pointcut = "execution(* com.portfolio.usermanagement.service.UserService.deleteUser(..))"
    )
    public void logUserDeletion(JoinPoint joinPoint) {
        String actor = getCurrentUsername();
        Object[] args = joinPoint.getArgs();
        String targetUserId = args.length > 0 ? args[0].toString() : "unknown";

        auditLogger.warn("USER_DELETION | actor={} | targetUserId={} | timestamp={} | ip={}",
            actor,
            targetUserId,
            Instant.now(),
            getClientInfo()
        );
    }

    /**
     * Log role changes.
     */
    @AfterReturning(
        pointcut = "execution(* com.portfolio.usermanagement.service.UserService.updateUserRoles(..))"
    )
    public void logRoleChange(JoinPoint joinPoint) {
        String actor = getCurrentUsername();
        Object[] args = joinPoint.getArgs();
        String targetUserId = args.length > 0 ? args[0].toString() : "unknown";

        auditLogger.warn("ROLE_CHANGE | actor={} | targetUserId={} | timestamp={} | ip={}",
            actor,
            targetUserId,
            Instant.now(),
            getClientInfo()
        );
    }

    /**
     * Log access to admin endpoints.
     */
    @Before("@annotation(org.springframework.security.access.prepost.PreAuthorize) " +
            "&& execution(* com.portfolio.usermanagement.controller..*(..))")
    public void logAdminAccess(JoinPoint joinPoint) {
        String actor = getCurrentUsername();
        String method = joinPoint.getSignature().toShortString();

        // Only log if user has ADMIN role
        if (hasRole("ROLE_ADMIN")) {
            auditLogger.info("ADMIN_ACCESS | actor={} | method={} | timestamp={} | ip={}",
                actor,
                method,
                Instant.now(),
                getClientInfo()
            );
        }
    }

    /**
     * Log failed authorization attempts.
     */
    @AfterThrowing(
        pointcut = "execution(* com.portfolio.usermanagement.controller..*(..))",
        throwing = "exception"
    )
    public void logAccessDenied(JoinPoint joinPoint, Exception exception) {
        if (exception.getClass().getSimpleName().contains("AccessDenied") ||
            exception.getClass().getSimpleName().contains("Forbidden")) {
            String actor = getCurrentUsername();
            String method = joinPoint.getSignature().toShortString();

            auditLogger.warn("ACCESS_DENIED | actor={} | method={} | timestamp={} | ip={} | reason={}",
                actor,
                method,
                Instant.now(),
                getClientInfo(),
                exception.getClass().getSimpleName()
            );
        }
    }

    // Helper methods

    /**
     * Extract username from method arguments (typically from DTO objects).
     * Uses reflection to call getUsername() on the first argument.
     */
    private String extractUsername(Object[] args) {
        if (args == null || args.length == 0) {
            return "unknown";
        }

        // Try to extract username from request DTOs (LoginRequest, RegisterRequest, etc.)
        Object firstArg = args[0];
        try {
            if (firstArg.getClass().getMethod("getUsername") != null) {
                Object username = firstArg.getClass().getMethod("getUsername").invoke(firstArg);
                return username != null ? username.toString() : "unknown";
            }
        } catch (Exception e) {
            return "unknown";
        }

        return "unknown";
    }

    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception e) {
            return "system";
        }
        return "anonymous";
    }

    private boolean hasRole(String role) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
        } catch (Exception e) {
            return false;
        }
    }

    private String getClientInfo() {
        // In a real application, extract this from HTTP request
        // For now, return placeholder
        return "client-ip-not-available";
    }
}
