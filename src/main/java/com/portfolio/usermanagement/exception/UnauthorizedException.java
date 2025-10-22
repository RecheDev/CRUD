package com.portfolio.usermanagement.exception;

/**
 * Exception thrown when authentication is required or has failed.
 * Maps to HTTP 401 Unauthorized.
 *
 * @author Portfolio Project
 */
public class UnauthorizedException extends RuntimeException {

    private final ErrorCode errorCode;

    public UnauthorizedException(String message) {
        super(message);
        this.errorCode = ErrorCode.AUTHENTICATION_REQUIRED;
    }

    public UnauthorizedException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
