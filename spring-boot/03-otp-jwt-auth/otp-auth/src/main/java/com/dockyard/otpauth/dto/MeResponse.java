package com.dockyard.otpauth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * MeResponse — the identity behind the current access token.
 *
 * Returned by the protected GET /auth/me endpoint. It reflects exactly what the
 * server trusts about the caller, including when their token expires — handy for
 * a client that wants to refresh proactively.
 */
@Data
@Builder
public class MeResponse {

    /** The authenticated identifier (the token subject). */
    private String subject;

    /** When the current access token was issued. */
    private Instant issuedAt;

    /** When the current access token expires. */
    private Instant expiresAt;

    /** Seconds until the access token expires. */
    private long expiresInSeconds;

    /** Granted authorities/roles. */
    private List<String> authorities;
}