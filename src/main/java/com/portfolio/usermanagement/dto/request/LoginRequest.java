package com.portfolio.usermanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login request.
 * Includes validation and automatic trimming of input.
 *
 * @author Portfolio Project
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @JsonProperty("username")
    private String username;

    @NotBlank(message = "Password is required")
    @JsonProperty("password")
    private String password;

    /**
     * Trim username field to remove leading/trailing whitespace.
     */
    public void trimFields() {
        if (username != null) username = username.trim();
    }

    /**
     * Get username in lowercase for case-insensitive comparison.
     *
     * @return lowercase username
     */
    public String getUsernameLowerCase() {
        return username != null ? username.toLowerCase() : null;
    }
}
