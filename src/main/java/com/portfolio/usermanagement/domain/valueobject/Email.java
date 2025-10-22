package com.portfolio.usermanagement.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.regex.Pattern;

/**
 * Value Object representing an email address.
 * Ensures email validity and immutability.
 *
 * @author Portfolio Project
 */
@Getter
@EqualsAndHashCode
public class Email {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    /**
     * Creates an Email value object from a string.
     *
     * @param email the email string
     * @return Email value object
     * @throws IllegalArgumentException if email is invalid
     */
    public static Email of(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }

        String trimmed = email.trim().toLowerCase();

        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("Email must not exceed 100 characters");
        }

        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }

        return new Email(trimmed);
    }

    /**
     * Get the domain part of the email.
     *
     * @return domain (e.g., "example.com")
     */
    public String getDomain() {
        int atIndex = value.indexOf('@');
        return atIndex >= 0 ? value.substring(atIndex + 1) : "";
    }

    /**
     * Get the local part of the email (before @).
     *
     * @return local part
     */
    public String getLocalPart() {
        int atIndex = value.indexOf('@');
        return atIndex >= 0 ? value.substring(0, atIndex) : value;
    }

    /**
     * Check if email belongs to a specific domain.
     *
     * @param domain the domain to check
     * @return true if email is from this domain
     */
    public boolean isFromDomain(String domain) {
        return getDomain().equalsIgnoreCase(domain);
    }

    @Override
    public String toString() {
        return value;
    }
}
