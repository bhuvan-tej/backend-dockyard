package com.dockyard.otpauth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * RefreshRequest — exchanges a valid refresh token for a new access token.
 */
@Data
public class RefreshRequest {

    @NotBlank(message = "refreshToken is required")
    private String refreshToken;
}