package com.portfolio.usermanagement.exception;

/**
 * Exception thrown when rate limit is exceeded.
 * Maps to HTTP 429 Too Many Requests.
 *
 * @author Portfolio Project
 */
public class TooManyRequestsException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Long retryAfterSeconds;

    public TooManyRequestsException(String message) {
        super(message);
        this.errorCode = ErrorCode.TOO_MANY_REQUESTS;
        this.retryAfterSeconds = null;
    }

    public TooManyRequestsException(String message, Long retryAfterSeconds) {
        super(message);
        this.errorCode = ErrorCode.RATE_LIMIT_EXCEEDED;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public TooManyRequestsException(String message, ErrorCode errorCode, Long retryAfterSeconds) {
        super(message);
        this.errorCode = errorCode;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
