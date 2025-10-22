package com.portfolio.usermanagement.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.regex.Pattern;

/**
 * Value Object representing a username.
 * Ensures username validity and immutability.
 *
 * @author Portfolio Project
 */
@Getter
@EqualsAndHashCode
public class Username {

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._-]{3,50}$"
    );

    private final String value;

    private Username(String value) {
        this.value = value;
    }

    /**
     * Creates a Username value object from a string.
     *
     * @param username the username string
     * @return Username value object
     * @throws IllegalArgumentException if username is invalid
     */
    public static Username of(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }

        String trimmed = username.trim();

        if (trimmed.length() < 3 || trimmed.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }

        if (!USERNAME_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                "Username can only contain letters, numbers, dots, underscores, and hyphens"
            );
        }

        return new Username(trimmed);
    }

    /**
     * Check if username contains only alphanumeric characters.
     *
     * @return true if alphanumeric only
     */
    public boolean isAlphanumeric() {
        return value.matches("^[a-zA-Z0-9]+$");
    }

    /**
     * Get the length of the username.
     *
     * @return username length
     */
    public int length() {
        return value.length();
    }

    @Override
    public String toString() {
        return value;
    }
}
