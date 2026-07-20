package com.dockyard.otpauth.security;

import java.time.Instant;

/**
 * ParsedToken - the trustworthy facts extracted from a verified JWT.
 *
 * Returned by {@link JwtService#parse}. Because it is only ever produced AFTER
 * the signature and expiry have been validated, the rest of the app can treat
 * these fields as authoritative.
 *
 * @param subject   who the token belongs to (the identifier)
 * @param tokenType ACCESS or REFRESH
 * @param tokenId   the JWT id ({@code jti}) - set on refresh tokens so they can
 *                  be tracked and rotated; null on access tokens
 * @param issuedAt  when it was minted
 * @param expiresAt when it stops being valid
 */
public record ParsedToken(
        String subject,
        TokenType tokenType,
        String tokenId,
        Instant issuedAt,
        Instant expiresAt
) {
    /** Seconds until expiry from now (never negative). */
    public long remainingSeconds() {
        long secs = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(secs, 0);
    }
}