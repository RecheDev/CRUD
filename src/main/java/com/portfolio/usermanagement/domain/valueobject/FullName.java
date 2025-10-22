package com.portfolio.usermanagement.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Value Object representing a person's full name.
 * Ensures name validity and immutability.
 *
 * @author Portfolio Project
 */
@Getter
@EqualsAndHashCode
public class FullName {

    private final String firstName;
    private final String lastName;

    private FullName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Creates a FullName value object.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @return FullName value object
     * @throws IllegalArgumentException if names are invalid
     */
    public static FullName of(String firstName, String lastName) {
        validateName(firstName, "First name");
        validateName(lastName, "Last name");

        return new FullName(
            capitalize(firstName.trim()),
            capitalize(lastName.trim())
        );
    }

    private static void validateName(String name, String fieldName) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }

        String trimmed = name.trim();

        if (trimmed.length() > 50) {
            throw new IllegalArgumentException(fieldName + " must not exceed 50 characters");
        }

        if (!trimmed.matches("^[a-zA-Z\\s'-]+$")) {
            throw new IllegalArgumentException(
                fieldName + " can only contain letters, spaces, hyphens, and apostrophes"
            );
        }
    }

    private static String capitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    /**
     * Get the full name as a single string.
     *
     * @return first name + space + last name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Get initials (e.g., "John Doe" -> "JD").
     *
     * @return initials in uppercase
     */
    public String getInitials() {
        return firstName.substring(0, 1).toUpperCase()
             + lastName.substring(0, 1).toUpperCase();
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
