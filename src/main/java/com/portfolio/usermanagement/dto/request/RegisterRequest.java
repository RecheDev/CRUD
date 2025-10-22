package com.portfolio.usermanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.portfolio.usermanagement.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user registration request.
 * Includes comprehensive validation and automatic trimming of input.
 *
 * @author Portfolio Project
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9._-]+$",
        message = "Username can only contain letters, numbers, dots, underscores, and hyphens"
    )
    @JsonProperty("username")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @JsonProperty("email")
    private String email;

    @NotBlank(message = "Password is required")
    @StrongPassword
    @JsonProperty("password")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Pattern(
        regexp = "^[a-zA-Z\\s'-]+$",
        message = "First name can only contain letters, spaces, hyphens, and apostrophes"
    )
    @JsonProperty("firstName")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Pattern(
        regexp = "^[a-zA-Z\\s'-]+$",
        message = "Last name can only contain letters, spaces, hyphens, and apostrophes"
    )
    @JsonProperty("lastName")
    private String lastName;

    /**
     * Trim all string fields to remove leading/trailing whitespace.
     * Should be called before validation.
     */
    public void trimFields() {
        if (username != null) username = username.trim();
        if (email != null) email = email.trim().toLowerCase();
        if (firstName != null) firstName = firstName.trim();
        if (lastName != null) lastName = lastName.trim();
    }

    /**
     * Get username in lowercase for case-insensitive comparison.
     *
     * @return lowercase username
     */
    public String getUsernameLowerCase() {
        return username != null ? username.toLowerCase() : null;
    }

    /**
     * Get email in lowercase (standard practice for emails).
     *
     * @return lowercase email
     */
    public String getEmailLowerCase() {
        return email != null ? email.toLowerCase() : null;
    }
}
