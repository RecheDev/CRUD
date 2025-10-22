package com.portfolio.usermanagement.constants;

/**
 * Centralized security-related constants to avoid magic strings throughout the codebase.
 * This improves maintainability and reduces the risk of typos.
 */
public final class SecurityConstants {

    private SecurityConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Role names
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // JWT-related constants
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    public static final String JWT_HEADER_STRING = "Authorization";
    public static final String JWT_TYPE_ACCESS = "access";
    public static final String JWT_TYPE_REFRESH = "refresh";
    public static final String JWT_ISSUER = "user-management-system";
    public static final String JWT_AUDIENCE = "api";

    // Account lockout configuration
    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    public static final int LOCKOUT_DURATION_MINUTES = 30;
    public static final int FAILED_ATTEMPTS_WINDOW_MINUTES = 15;

    // Rate limiting configuration
    public static final int MAX_REQUESTS_PER_MINUTE = 60;
    public static final int MAX_REQUESTS_PER_HOUR = 1000;
    public static final int RATE_LIMIT_CLEANUP_HOURS = 24;

    // Password requirements
    public static final int PASSWORD_MIN_LENGTH = 12;
    public static final String PASSWORD_REQUIREMENTS_MESSAGE =
            "Password must be at least " + PASSWORD_MIN_LENGTH + " characters long and contain " +
            "at least one uppercase letter, one lowercase letter, one digit, and one special character";

    // Username requirements
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 50;

    // Scheduled task intervals (in milliseconds)
    public static final long CLEANUP_INTERVAL_MS = 3600000; // 1 hour

    // Token expiration (in milliseconds)
    public static final long DEFAULT_ACCESS_TOKEN_EXPIRATION_MS = 86400000; // 24 hours
    public static final long DEFAULT_REFRESH_TOKEN_EXPIRATION_MS = 604800000; // 7 days
}
