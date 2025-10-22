package com.portfolio.usermanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for logout endpoint.
 * Requires the refresh token to revoke it from the database.
 * The access token is automatically blacklisted via the Authorization header.
 *
 * @author Portfolio Project
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {

    /**
     * The refresh token to revoke.
     * The access token will be blacklisted separately via JWT ID.
     */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
