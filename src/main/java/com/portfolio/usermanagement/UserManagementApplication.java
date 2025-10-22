package com.portfolio.usermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for User Management System.
 *
 * This Spring Boot application provides a comprehensive CRUD system for user management
 * with features including:
 * - JWT-based authentication and authorization
 * - Role-based access control (RBAC)
 * - Automatic entity auditing
 * - PostgreSQL database with Flyway migrations
 * - RESTful API with OpenAPI documentation
 * - Comprehensive testing (unit, integration, and API tests)
 *
 * @author Portfolio Project
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableScheduling
public class UserManagementApplication {

    /**
     * Main entry point for the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(UserManagementApplication.class, args);
    }
}
