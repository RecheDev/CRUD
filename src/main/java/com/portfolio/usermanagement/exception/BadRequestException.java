package com.portfolio.usermanagement.exception;

/**
 * Exception thrown when the client request is invalid.
 * Maps to HTTP 400 Bad Request.
 *
 * @author Portfolio Project
 */
public class BadRequestException extends RuntimeException {

    private final ErrorCode errorCode;

    public BadRequestException(String message) {
        super(message);
        this.errorCode = ErrorCode.INVALID_INPUT;
    }

    public BadRequestException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
