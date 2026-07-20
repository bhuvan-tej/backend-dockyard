package com.dockyard.otpauth.security;

import com.dockyard.otpauth.config.JwtProperties;
import com.dockyard.otpauth.exception.InvalidTokenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JwtServiceTest — round-trips, type enforcement, expiry and tamper detection.
 *
 * This is the security-critical class, so it gets the most scrutiny.
 */
class JwtServiceTest {

    private static final String SECRET = "test-secret-that-is-definitely-long-enough-32bytes!!";

    private JwtProperties props(long accessTtl, long refreshTtl) {
        JwtProperties p = new JwtProperties();
        p.setSecret(SECRET);
        p.setIssuer("dockyard-test");
        p.setAccessTokenTtlSeconds(accessTtl);
        p.setRefreshTokenTtlSeconds(refreshTtl);
        return p;
    }

    @Test
    @DisplayName("access token round-trips: subject and type survive")
    void accessTokenRoundTrip() {
        JwtService service = new JwtService(props(900, 604800));

        String token = service.issueAccessToken("ada@x.io");
        ParsedToken parsed = service.parse(token, TokenType.ACCESS);

        assertThat(parsed.subject()).isEqualTo("ada@x.io");
        assertThat(parsed.tokenType()).isEqualTo(TokenType.ACCESS);
        assertThat(parsed.remainingSeconds()).isPositive();
    }

    @Test
    @DisplayName("refresh token parsed as ACCESS is rejected (wrong type)")
    void wrongTypeIsRejected() {
        JwtService service = new JwtService(props(900, 604800));

        String refresh = service.issueRefreshToken("ada@x.io", "jti-1");

        assertThatThrownBy(() -> service.parse(refresh, TokenType.ACCESS))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Expected a ACCESS token");
    }

    @Test
    @DisplayName("expired token is rejected")
    void expiredTokenIsRejected() {
        // Negative TTL → the token is born already expired.
        JwtService service = new JwtService(props(-1, -1));

        String token = service.issueAccessToken("ada@x.io");

        assertThatThrownBy(() -> service.parse(token, TokenType.ACCESS))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("tampered token is rejected")
    void tamperedTokenIsRejected() {
        JwtService service = new JwtService(props(900, 604800));

        String token = service.issueAccessToken("ada@x.io");
        // Flip the last character of the signature.
        String tampered = token.substring(0, token.length() - 1)
                + (token.charAt(token.length() - 1) == 'A' ? 'B' : 'A');

        assertThatThrownBy(() -> service.parse(tampered, TokenType.ACCESS))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("a token signed with a different secret is rejected")
    void foreignSecretIsRejected() {
        JwtService issuer = new JwtService(props(900, 604800));
        JwtProperties otherProps = props(900, 604800);
        otherProps.setSecret("a-completely-different-secret-key-32bytes-minimum!!");
        JwtService verifier = new JwtService(otherProps);

        String token = issuer.issueAccessToken("ada@x.io");

        assertThatThrownBy(() -> verifier.parse(token, TokenType.ACCESS))
                .isInstanceOf(InvalidTokenException.class);
    }
}

