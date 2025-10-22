package com.portfolio.usermanagement.exception;

/**
 * Enum representing specific error codes for client-side error handling.
 * These codes provide machine-readable error identifiers.
 *
 * @author Portfolio Project
 */
public enum ErrorCode {
    // Resource errors (404)
    USER_NOT_FOUND("USER_NOT_FOUND", "User resource not found"),
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", "Role resource not found"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Requested resource not found"),

    // Authentication errors (401)
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid username or password"),
    INVALID_TOKEN("INVALID_TOKEN", "Invalid or expired authentication token"),
    AUTHENTICATION_REQUIRED("AUTHENTICATION_REQUIRED", "Authentication is required"),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Authentication token has expired"),

    // Authorization errors (403)
    ACCESS_DENIED("ACCESS_DENIED", "Access to this resource is denied"),
    INSUFFICIENT_PERMISSIONS("INSUFFICIENT_PERMISSIONS", "Insufficient permissions for this operation"),

    // Validation errors (400, 422)
    VALIDATION_FAILED("VALIDATION_FAILED", "Input validation failed"),
    INVALID_INPUT("INVALID_INPUT", "Invalid input data provided"),
    MISSING_REQUIRED_FIELD("MISSING_REQUIRED_FIELD", "Required field is missing"),

    // Business logic errors (400)
    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS", "Username is already taken"),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Email is already in use"),
    INVALID_OPERATION("INVALID_OPERATION", "Invalid operation requested"),

    // Conflict errors (409)
    RESOURCE_ALREADY_EXISTS("RESOURCE_ALREADY_EXISTS", "Resource already exists"),
    DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "Duplicate resource detected"),

    // Account security errors (423)
    ACCOUNT_LOCKED("ACCOUNT_LOCKED", "Account is locked due to security reasons"),
    ACCOUNT_DISABLED("ACCOUNT_DISABLED", "Account is disabled"),
    ACCOUNT_EXPIRED("ACCOUNT_EXPIRED", "Account has expired"),
    CREDENTIALS_EXPIRED("CREDENTIALS_EXPIRED", "Credentials have expired"),

    // Rate limiting errors (429)
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS", "Too many requests, please try again later"),
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "Rate limit exceeded"),

    // Server errors (500)
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "An internal server error occurred"),
    DATABASE_ERROR("DATABASE_ERROR", "Database operation failed"),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "Service temporarily unavailable");

    private final String code;
    private final String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code;
    }
}
