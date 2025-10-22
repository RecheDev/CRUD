package com.portfolio.usermanagement.exception;

/**
 * Exception thrown when a resource conflict occurs (e.g., duplicate resource).
 * Maps to HTTP 409 Conflict.
 *
 * @author Portfolio Project
 */
public class ConflictException extends RuntimeException {

    private final ErrorCode errorCode;

    public ConflictException(String message) {
        super(message);
        this.errorCode = ErrorCode.RESOURCE_ALREADY_EXISTS;
    }

    public ConflictException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
