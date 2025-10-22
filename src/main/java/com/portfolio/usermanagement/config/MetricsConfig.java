package com.portfolio.usermanagement.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for custom metrics with Micrometer.
 * Provides application-level metrics tracking for monitoring and observability.
 */
@Configuration
public class MetricsConfig {

    /**
     * Customizes the MeterRegistry with application-specific tags.
     * These tags will be added to all metrics exported to Prometheus.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags(
                        "application", "user-management-system",
                        "environment", System.getProperty("spring.profiles.active", "dev")
                );
    }

    /**
     * Enables @Timed annotation support for method-level timing metrics.
     * This allows methods annotated with @Timed to be automatically tracked.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Creates custom counters for tracking business metrics.
     */
    @Bean
    public Counter userRegistrationCounter(MeterRegistry registry) {
        return Counter.builder("user.registration.total")
                .description("Total number of user registrations")
                .tag("type", "registration")
                .register(registry);
    }

    @Bean
    public Counter loginSuccessCounter(MeterRegistry registry) {
        return Counter.builder("user.login.success")
                .description("Total number of successful login attempts")
                .tag("type", "login")
                .register(registry);
    }

    @Bean
    public Counter loginFailureCounter(MeterRegistry registry) {
        return Counter.builder("user.login.failure")
                .description("Total number of failed login attempts")
                .tag("type", "login")
                .register(registry);
    }

    /**
     * Creates a timer for tracking authentication request duration.
     */
    @Bean
    public Timer authenticationTimer(MeterRegistry registry) {
        return Timer.builder("user.authentication.duration")
                .description("Duration of authentication requests")
                .tag("operation", "authentication")
                .register(registry);
    }
}
