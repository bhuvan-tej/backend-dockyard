package com.dockyard.otpauth.dto;

import lombok.Builder;
import lombok.Data;

/**
 * TokenResponse — the pair of JWTs handed out after a successful OTP verify or
 * refresh.
 *
 * Two tokens, two lifetimes:
 *   accessToken  — short-lived; sent on every request as "Authorization: Bearer".
 *   refreshToken — long-lived; used only to obtain a new access token.
 */
@Data
@Builder
public class TokenResponse {

    /** Always "Bearer" — how the access token is presented in the header. */
    private String tokenType;

    private String accessToken;
    private String refreshToken;

    /** Access-token validity in seconds (how long until it expires). */
    private long accessTokenExpiresInSeconds;

    /** Refresh-token validity in seconds. */
    private long refreshTokenExpiresInSeconds;

    /** Who the tokens belong to (the identifier). */
    private String subject;
}