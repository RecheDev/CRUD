package com.portfolio.usermanagement.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Maps to HTTP 404 Not Found.
 *
 * @author Portfolio Project
 */
public class ResourceNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;

    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = ErrorCode.RESOURCE_NOT_FOUND;
    }

    public ResourceNotFoundException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.errorCode = determineErrorCode(resourceName);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    private ErrorCode determineErrorCode(String resourceName) {
        return switch (resourceName.toLowerCase()) {
            case "user" -> ErrorCode.USER_NOT_FOUND;
            case "role" -> ErrorCode.ROLE_NOT_FOUND;
            default -> ErrorCode.RESOURCE_NOT_FOUND;
        };
    }
}
