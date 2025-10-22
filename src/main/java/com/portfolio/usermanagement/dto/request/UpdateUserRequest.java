package com.portfolio.usermanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating user information.
 * All fields are optional - only provided fields will be updated.
 *
 * @author Portfolio Project
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @JsonProperty("email")
    private String email;

    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Pattern(
        regexp = "^[a-zA-Z\\s'-]+$",
        message = "First name can only contain letters, spaces, hyphens, and apostrophes"
    )
    @JsonProperty("firstName")
    private String firstName;

    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Pattern(
        regexp = "^[a-zA-Z\\s'-]+$",
        message = "Last name can only contain letters, spaces, hyphens, and apostrophes"
    )
    @JsonProperty("lastName")
    private String lastName;

    /**
     * Trim all string fields to remove leading/trailing whitespace.
     */
    public void trimFields() {
        if (email != null) email = email.trim().toLowerCase();
        if (firstName != null) firstName = firstName.trim();
        if (lastName != null) lastName = lastName.trim();
    }

    /**
     * Check if any field is provided for update.
     *
     * @return true if at least one field is not null
     */
    public boolean hasUpdates() {
        return email != null || firstName != null || lastName != null;
    }

    /**
     * Get email in lowercase.
     *
     * @return lowercase email
     */
    public String getEmailLowerCase() {
        return email != null ? email.toLowerCase() : null;
    }
}
