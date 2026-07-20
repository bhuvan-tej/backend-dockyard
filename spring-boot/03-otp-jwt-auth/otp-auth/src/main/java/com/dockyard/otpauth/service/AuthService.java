package com.dockyard.otpauth.service;

import com.dockyard.otpauth.dto.RefreshRequest;
import com.dockyard.otpauth.dto.TokenResponse;
import com.dockyard.otpauth.entity.RefreshToken;
import com.dockyard.otpauth.exception.InvalidTokenException;
import com.dockyard.otpauth.repository.RefreshTokenRepository;
import com.dockyard.otpauth.security.JwtService;
import com.dockyard.otpauth.security.ParsedToken;
import com.dockyard.otpauth.security.TokenType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService - token lifecycle operations that do not involve an OTP.
 *
 * refresh() implements ROTATION with REUSE DETECTION, the model used by Auth0,
 * Okta and Cognito. Each refresh token works exactly once; using it mints a new
 * pair and revokes the old one. Replaying an already-used token is treated as a
 * theft signal and revokes the whole session family.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final TokenIssuer tokenIssuer;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Refresh with rotation and reuse detection:
     *   1. validate the refresh JWT (signature, issuer, expiry, type)
     *   2. its jti must exist in the store
     *   3. if that jti is already revoked -> replay -> revoke the whole subject
     *      family and reject (suspected theft)
     *   4. otherwise revoke the old token (rotate) and issue a brand-new pair
     */
    @Transactional(noRollbackFor = InvalidTokenException.class)
    public TokenResponse refresh(RefreshRequest request) {
        ParsedToken parsed = jwtService.parse(request.getRefreshToken(), TokenType.REFRESH);

        RefreshToken stored = refreshTokenRepository.findByTokenId(parsed.tokenId())
                .orElseThrow(() -> new InvalidTokenException("Refresh token is not recognized"));

        if (stored.isRevoked()) {
            int revoked = refreshTokenRepository.revokeAllForSubject(stored.getSubject());
            log.warn("Refresh-token reuse detected for {} - revoked {} token(s)", stored.getSubject(), revoked);
            throw new InvalidTokenException("Refresh token has already been used - all sessions revoked");
        }

        // Rotate: the presented token is now spent, a new one is issued below.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        log.info("Rotating tokens for subject {}", parsed.subject());
        return tokenIssuer.issueFor(parsed.subject());
    }
}