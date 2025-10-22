package com.portfolio.usermanagement.api;

import com.portfolio.usermanagement.dto.request.LoginRequest;
import com.portfolio.usermanagement.dto.request.RegisterRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class AuthApiTest {

    @LocalServerPort
    private int port;

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
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";
    }

    @Test
    void shouldRegisterNewUser() {
        RegisterRequest request = RegisterRequest.builder()
                .username("apitest")
                .email("apitest@example.com")
                .password("Password123!")
                .firstName("API")
                .lastName("Test")
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(201)
            .body("token", notNullValue())
            .body("type", equalTo("Bearer"))
            .body("user.username", equalTo("apitest"))
            .body("user.email", equalTo("apitest@example.com"))
            .body("user.firstName", equalTo("API"))
            .body("user.lastName", equalTo("Test"))
            .body("user.enabled", equalTo(true));
    }

    @Test
    void shouldNotRegisterUserWithExistingUsername() {
        RegisterRequest firstRequest = RegisterRequest.builder()
                .username("duplicate")
                .email("first@example.com")
                .password("Password123!")
                .firstName("First")
                .lastName("User")
                .build();

        // Register first user
        given()
            .contentType(ContentType.JSON)
            .body(firstRequest)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(201);

        // Try to register with same username
        RegisterRequest secondRequest = RegisterRequest.builder()
                .username("duplicate")
                .email("second@example.com")
                .password("Password123!")
                .firstName("Second")
                .lastName("User")
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(secondRequest)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(409)
            .body("message", containsString("Username is already taken"));
    }

    @Test
    void shouldLoginWithValidCredentials() {
        // First register a user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("logintest")
                .email("logintest@example.com")
                .password("Password123!")
                .firstName("Login")
                .lastName("Test")
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(registerRequest)
        .when()
            .post("/auth/register");

        // Then login
        LoginRequest loginRequest = LoginRequest.builder()
                .username("logintest")
                .password("Password123!")
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("type", equalTo("Bearer"))
            .body("user.username", equalTo("logintest"));
    }

    @Test
    void shouldNotLoginWithInvalidCredentials() {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("nonexistent")
                .password("wrongpassword")
                .build();

        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(401);
    }
}
