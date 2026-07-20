package com.dockyard.otpauth.service;

import com.dockyard.otpauth.config.JwtProperties;
import com.dockyard.otpauth.dto.TokenResponse;
import com.dockyard.otpauth.entity.RefreshToken;
import com.dockyard.otpauth.repository.RefreshTokenRepository;
import com.dockyard.otpauth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TokenIssuer - the one place that assembles a {@link TokenResponse}.
 *
 * Both the OTP-verify flow and the refresh flow hand out a fresh access+refresh
 * pair here, so the token shape and lifetimes are defined exactly once. It also
 * PERSISTS each refresh token (by its jti) so the token can later be rotated.
 */
@Component
@RequiredArgsConstructor
public class TokenIssuer {

    private static final String BEARER = "Bearer";

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    /** Mints a new access+refresh pair for the subject and tracks the refresh token. */
    public TokenResponse issueFor(String subject) {
        String accessToken = jwtService.issueAccessToken(subject);

        // The refresh token gets a unique jti we also persist, so it can later be
        // rotated (revoked) - the one thing a bare stateless JWT cannot do.
        String tokenId = UUID.randomUUID().toString();
        String refreshToken = jwtService.issueRefreshToken(subject, tokenId);

        refreshTokenRepository.save(RefreshToken.builder()
                .tokenId(tokenId)
                .subject(subject)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenTtlSeconds()))
                .revoked(false)
                .build());

        return TokenResponse.builder()
                .tokenType(BEARER)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresInSeconds(jwtProperties.getAccessTokenTtlSeconds())
                .refreshTokenExpiresInSeconds(jwtProperties.getRefreshTokenTtlSeconds())
                .subject(subject)
                .build();
    }
}