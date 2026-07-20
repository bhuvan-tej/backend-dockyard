package com.dockyard.otpauth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JwtProperties — signing secret, issuer and the two token lifetimes, bound from
 * the {@code jwt.*} block in application.yml.
 *
 * The access token is deliberately short-lived and the refresh token long-lived:
 * if an access token leaks it is only dangerous for minutes, while the user still
 * enjoys a long session via the refresh token.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * HMAC-SHA256 signing secret. MUST be at least 32 bytes (256 bits).
     * Override in production via the JWT_SECRET environment variable.
     */
    private String secret;

    /** The {@code iss} claim; parsing also requires this exact issuer. */
    private String issuer = "dockyard-otp-auth";

    /** Access-token lifetime in seconds (short — e.g. 15 minutes). */
    private long accessTokenTtlSeconds = 900;

    /** Refresh-token lifetime in seconds (long — e.g. 7 days). */
    private long refreshTokenTtlSeconds = 604800;
}