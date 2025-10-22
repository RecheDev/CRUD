package com.portfolio.usermanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for refreshing access tokens.
 *
 * @author Portfolio Project
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    /**
     * The refresh token to exchange for a new access token.
     */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
