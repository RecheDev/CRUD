package com.portfolio.usermanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration for enabling AOP-based audit logging.
 */
@Configuration
@EnableAspectJAutoProxy
public class AuditConfig {
    // AOP is now enabled for the application
}
