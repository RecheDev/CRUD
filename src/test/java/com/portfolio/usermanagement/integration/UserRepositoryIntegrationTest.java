package com.portfolio.usermanagement.integration;

import com.portfolio.usermanagement.config.AuditorAwareImpl;
import com.portfolio.usermanagement.entity.Role;
import com.portfolio.usermanagement.entity.User;
import com.portfolio.usermanagement.repository.RoleRepository;
import com.portfolio.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(AuditorAwareImpl.class)
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> false);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role userRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        userRole = new Role(Role.RoleName.ROLE_USER, "Standard user");
        userRole = roleRepository.save(userRole);
    }

    @Test
    void shouldSaveAndFindUserByUsername() {
        // Arrange
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .username("integrationtest")
                .email("integration@test.com")
                .password("hashedPassword")
                .firstName("Integration")
                .lastName("Test")
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .roles(roles)
                .build();

        // Act
        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findByUsername("integrationtest");

        // Assert
        assertThat(savedUser.getId()).isNotNull();
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("integrationtest");
        assertThat(foundUser.get().getEmail()).isEqualTo("integration@test.com");
        assertThat(foundUser.get().getRoles()).hasSize(1);
    }

    @Test
    void shouldReturnTrueWhenUsernameExists() {
        // Arrange
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .username("existinguser")
                .email("existing@test.com")
                .password("hashedPassword")
                .firstName("Existing")
                .lastName("User")
                .enabled(true)
                .accountNonLocked(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .roles(roles)
                .build();

        userRepository.save(user);

        // Act
        boolean exists = userRepository.existsByUsername("existinguser");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUsernameDoesNotExist() {
        // Act
        boolean exists = userRepository.existsByUsername("nonexistentuser");

        // Assert
        assertThat(exists).isFalse();
    }
}
