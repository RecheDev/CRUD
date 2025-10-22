package com.portfolio.usermanagement.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Service for recording custom application metrics.
 * Provides methods to track business operations and performance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    /**
     * Records a successful user registration event.
     */
    public void recordUserRegistration() {
        Counter.builder("user.registration.total")
                .description("Total number of user registrations")
                .tag("status", "success")
                .register(meterRegistry)
                .increment();
        log.debug("User registration metric recorded");
    }

    /**
     * Records a successful login event.
     */
    public void recordLoginSuccess(String username) {
        Counter.builder("user.login.attempts")
                .description("Login attempts")
                .tag("status", "success")
                .tag("user", username)
                .register(meterRegistry)
                .increment();
        log.debug("Successful login metric recorded for user: {}", username);
    }

    /**
     * Records a failed login event.
     */
    public void recordLoginFailure(String username, String reason) {
        Counter.builder("user.login.attempts")
                .description("Login attempts")
                .tag("status", "failure")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
        log.debug("Failed login metric recorded for user: {} with reason: {}", username, reason);
    }

    /**
     * Records the duration of an authentication operation.
     */
    public <T> T recordAuthenticationDuration(Supplier<T> operation) {
        Timer timer = Timer.builder("user.authentication.duration")
                .description("Duration of authentication operations")
                .register(meterRegistry);

        return timer.record(operation);
    }

    /**
     * Records a database query execution time.
     */
    public void recordDatabaseQueryTime(String queryName, long durationMs) {
        Timer.builder("database.query.duration")
                .description("Database query execution time")
                .tag("query", queryName)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Records an API endpoint call.
     */
    public void recordApiCall(String endpoint, String method, int statusCode) {
        Counter.builder("api.calls.total")
                .description("Total API calls")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("status", String.valueOf(statusCode))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records a security event (e.g., account lockout, suspicious activity).
     */
    public void recordSecurityEvent(String eventType) {
        Counter.builder("security.events")
                .description("Security-related events")
                .tag("type", eventType)
                .register(meterRegistry)
                .increment();
        log.warn("Security event recorded: {}", eventType);
    }

    /**
     * Records a rate limit violation.
     */
    public void recordRateLimitViolation(String identifier) {
        Counter.builder("rate.limit.violations")
                .description("Rate limit violations")
                .tag("identifier", identifier)
                .register(meterRegistry)
                .increment();
        log.warn("Rate limit violation recorded for: {}", identifier);
    }
}
