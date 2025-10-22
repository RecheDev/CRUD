package com.portfolio.usermanagement.config;

import com.portfolio.usermanagement.entity.Role;
import com.portfolio.usermanagement.entity.User;
import com.portfolio.usermanagement.repository.RoleRepository;
import com.portfolio.usermanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Secure admin initialization component.
 * Creates an admin user only if:
 * 1. Environment variable ADMIN_USERNAME and ADMIN_PASSWORD are set
 * 2. No admin user exists yet
 * 3. Credentials meet security requirements
 */
@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.username:#{null}}")
    private String adminUsername;

    @Value("${admin.password:#{null}}")
    private String adminPassword;

    @Value("${admin.email:#{null}}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        // Only create admin if explicitly configured and doesn't exist
        if (adminUsername == null || adminPassword == null) {
            logger.info("Admin initialization skipped - no credentials provided");
            logger.info("To create admin user, set environment variables: ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_EMAIL");
            return;
        }

        if (userRepository.existsByUsername(adminUsername)) {
            logger.info("Admin user already exists - skipping initialization");
            return;
        }

        // Validate password strength
        if (adminPassword.length() < 12) {
            logger.error("Admin password must be at least 12 characters - initialization aborted");
            return;
        }

        try {
            Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found in database"));

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            User admin = User.builder()
                    .id(UUID.randomUUID())
                    .username(adminUsername)
                    .email(adminEmail != null ? adminEmail : adminUsername + "@system.local")
                    .password(passwordEncoder.encode(adminPassword))
                    .firstName("System")
                    .lastName("Administrator")
                    .enabled(true)
                    .accountNonLocked(true)
                    .accountNonExpired(true)
                    .credentialsNonExpired(true)
                    .roles(roles)
                    .build();

            userRepository.save(admin);
            logger.info("Admin user created successfully: {}", adminUsername);
            logger.warn("IMPORTANT: Change the admin password immediately after first login!");
        } catch (Exception e) {
            logger.error("Failed to create admin user: {}", e.getMessage());
        }
    }
}
