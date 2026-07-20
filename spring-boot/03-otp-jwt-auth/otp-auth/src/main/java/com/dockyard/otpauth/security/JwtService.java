package com.dockyard.otpauth.security;

import com.dockyard.otpauth.config.JwtProperties;
import com.dockyard.otpauth.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JwtService - the ONLY class that imports the JJWT library.
 *
 * WHY ISOLATE IT (exactly like QrCodeEngine isolates ZXing)?
 *   - The filter, controllers and service depend on our own {@link ParsedToken},
 *     not on io.jsonwebtoken types.
 *   - Swapping JWT libraries (or moving to opaque tokens) touches one file.
 *   - JJWT exceptions (expired, malformed, bad signature) are translated into
 *     our {@link InvalidTokenException} right here, at the boundary.
 *
 * Refresh tokens carry a JWT id ({@code jti}) so they can be tracked in the
 * database and rotated; access tokens stay stateless and carry no jti.
 *
 * Tokens are signed with symmetric HMAC-SHA. JJWT selects the strongest variant
 * the secret length allows (HS256/HS384/HS512).
 */
@Slf4j
@Component
public class JwtService {

    private static final String CLAIM_TYPE = "type";

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        // HMAC-SHA requires a >= 256-bit (32-byte) key. A short secret throws here
        // at startup - failing fast beats minting weakly-signed tokens.
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /** Mints a short-lived ACCESS token (no jti - access tokens are stateless). */
    public String issueAccessToken(String subject) {
        return issue(subject, null, TokenType.ACCESS, properties.getAccessTokenTtlSeconds());
    }

    /** Mints a long-lived REFRESH token stamped with the given jti for tracking. */
    public String issueRefreshToken(String subject, String tokenId) {
        return issue(subject, tokenId, TokenType.REFRESH, properties.getRefreshTokenTtlSeconds());
    }

    private String issue(String subject, String tokenId, TokenType type, long ttlSeconds) {
        Instant now = Instant.now();
        Instant expiry = now.plus(ttlSeconds, ChronoUnit.SECONDS);

        var builder = Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(subject)
                .claim(CLAIM_TYPE, type.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry));
        if (tokenId != null) {
            builder.id(tokenId); // sets the standard jti claim
        }
        return builder.signWith(key).compact();
    }

    /**
     * Verifies a token signature, issuer and expiry, then returns the facts.
     * A wrong type (e.g. a refresh token where an access token is required) is
     * rejected too.
     *
     * @throws InvalidTokenException if anything about the token is not trustworthy
     */
    public ParsedToken parse(String token, TokenType expectedType) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(properties.getIssuer())
                    .build()
                    .parseSignedClaims(token);

            Claims claims = jws.getPayload();
            TokenType actualType = TokenType.valueOf(String.valueOf(claims.get(CLAIM_TYPE)));
            if (actualType != expectedType) {
                throw new InvalidTokenException(
                        "Expected a " + expectedType + " token but received a " + actualType + " token");
            }

            return new ParsedToken(
                    claims.getSubject(),
                    actualType,
                    claims.getId(),
                    claims.getIssuedAt().toInstant(),
                    claims.getExpiration().toInstant());

        } catch (ExpiredJwtException ex) {
            throw new InvalidTokenException("Token has expired", ex);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException("Token is invalid", ex);
        }
    }
}