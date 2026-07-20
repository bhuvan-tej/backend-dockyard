package com.dockyard.otpauth.controller;

import com.dockyard.otpauth.dto.MeResponse;
import com.dockyard.otpauth.dto.RefreshRequest;
import com.dockyard.otpauth.dto.TokenResponse;
import com.dockyard.otpauth.security.JwtService;
import com.dockyard.otpauth.security.ParsedToken;
import com.dockyard.otpauth.security.TokenType;
import com.dockyard.otpauth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AuthController — token lifecycle and identity.
 *
 * Full base path is /api/auth.
 *   POST /refresh  → PUBLIC: swap a refresh token for a new access token.
 *   GET  /me       → PROTECTED: requires a valid Bearer access token.
 *
 * /me proves the JWT security actually works: without a good token the request
 * never reaches this method (the entry point returns 401 first).
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Refresh tokens and inspect the current identity")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;
    private final JwtService jwtService;

    /**
     * POST /api/auth/refresh
     * Issues a fresh access + refresh pair from a valid refresh token.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh tokens",
            description = "Exchanges a valid, unexpired refresh token for a new access + refresh pair.")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        log.info("POST /auth/refresh");
        return ResponseEntity.ok(authService.refresh(request));
    }

    /**
     * GET /api/auth/me
     * Returns the identity behind the current access token (including its expiry).
     */
    @GetMapping("/me")
    @Operation(summary = "Who am I?",
            description = "Returns the subject and token expiry for the current access token. Requires authentication.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MeResponse> me(@RequestHeader("Authorization") String authorization,
                                         Authentication authentication) {
        // The security filter already validated this token; we re-parse only to
        // surface its issued/expiry timestamps to the caller.
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        ParsedToken parsed = jwtService.parse(token, TokenType.ACCESS);

        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(MeResponse.builder()
                .subject(parsed.subject())
                .issuedAt(parsed.issuedAt())
                .expiresAt(parsed.expiresAt())
                .expiresInSeconds(parsed.remainingSeconds())
                .authorities(authorities)
                .build());
    }
}