package com.portfolio.usermanagement.exception;

/**
 * Exception thrown when user is authenticated but lacks permission for the requested resource.
 * Maps to HTTP 403 Forbidden.
 *
 * @author Portfolio Project
 */
public class ForbiddenException extends RuntimeException {

    private final ErrorCode errorCode;

    public ForbiddenException(String message) {
        super(message);
        this.errorCode = ErrorCode.ACCESS_DENIED;
    }

    public ForbiddenException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
