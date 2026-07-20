package com.dockyard.otpauth.service;

import com.dockyard.otpauth.config.OtpProperties;
import com.dockyard.otpauth.dto.OtpChallengeResponse;
import com.dockyard.otpauth.dto.OtpRequest;
import com.dockyard.otpauth.dto.OtpVerifyRequest;
import com.dockyard.otpauth.dto.TokenResponse;
import com.dockyard.otpauth.entity.OtpChallenge;
import com.dockyard.otpauth.exception.ChallengeNotFoundException;
import com.dockyard.otpauth.exception.InvalidOtpException;
import com.dockyard.otpauth.exception.OtpExpiredException;
import com.dockyard.otpauth.exception.TooManyAttemptsException;
import com.dockyard.otpauth.repository.OtpChallengeRepository;
import com.dockyard.otpauth.security.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * OtpService — all business logic for issuing and verifying one-time passwords.
 *
 * It orchestrates the pieces but delegates the specialised work:
 *   OtpGenerator  → makes the secure random code
 *   PasswordEncoder → hashes it (we never store the code itself)
 *   TokenIssuer   → mints JWTs on success
 *
 * VALIDITY & EXPIRY are enforced here, in order:
 *   1. resend cooldown  — cannot spam new codes
 *   2. challenge exists  — 404 otherwise
 *   3. not consumed      — single use
 *   4. not expired       — validity window
 *   5. not locked        — attempt limit
 *   6. code matches      — else burn an attempt
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpChallengeRepository repository;
    private final OtpGenerator otpGenerator;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;
    private final OtpProperties properties;

    /**
     * Generates a new OTP for the identifier, stores its hash with an expiry,
     * and "delivers" it (logged; returned only in dev mode).
     */
    @Transactional
    public OtpChallengeResponse requestOtp(OtpRequest request) {
        enforceResendCooldown(request);

        String code = otpGenerator.generate();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(properties.getTtlSeconds());

        OtpChallenge challenge = OtpChallenge.builder()
                .challengeId(UUID.randomUUID().toString())
                .identifier(request.getIdentifier())
                .purpose(request.getPurpose())
                .channel(request.getChannel())
                .codeHash(passwordEncoder.encode(code)) // store the HASH, not the code
                .expiresAt(expiresAt)
                .attempts(0)
                .maxAttempts(properties.getMaxAttempts())
                .consumed(false)
                .build();

        repository.save(challenge);

        // In a real system this is where an SMS/email would go out. We log it so
        // the flow is observable, and (dev only) echo it in the response.
        log.info("OTP for '{}' [{}]: {} (valid {}s)",
                request.getIdentifier(), request.getPurpose(), code, properties.getTtlSeconds());

        return OtpChallengeResponse.builder()
                .challengeId(challenge.getChallengeId())
                .identifier(request.getIdentifier())
                .purpose(request.getPurpose())
                .channel(request.getChannel())
                .expiresAt(expiresAt)
                .ttlSeconds(properties.getTtlSeconds())
                .maxAttempts(properties.getMaxAttempts())
                .resendAfterSeconds(properties.getResendCooldownSeconds())
                .message("OTP generated for " + mask(request.getIdentifier()))
                .devCode(properties.isExposeCode() ? code : null)
                .build();
    }

    /**
     * Verifies a code against a challenge. On success the challenge is consumed
     * (single-use) and a JWT access+refresh pair is returned.
     */
    @Transactional
    public TokenResponse verify(OtpVerifyRequest request) {
        OtpChallenge challenge = repository.findByChallengeId(request.getChallengeId())
                .orElseThrow(() -> new ChallengeNotFoundException(
                        "No OTP challenge found for the supplied challengeId"));

        if (challenge.isConsumed()) {
            throw new InvalidOtpException("This OTP has already been used", 0);
        }
        if (challenge.isExpired(LocalDateTime.now())) {
            throw new OtpExpiredException("This OTP has expired — request a new one");
        }
        if (challenge.isLocked()) {
            throw new TooManyAttemptsException("Too many incorrect attempts — request a new OTP");
        }

        if (!passwordEncoder.matches(request.getCode(), challenge.getCodeHash())) {
            challenge.setAttempts(challenge.getAttempts() + 1);
            int remaining = Math.max(challenge.getMaxAttempts() - challenge.getAttempts(), 0);

            // Burn the challenge once the limit is hit so it cannot be retried.
            if (challenge.isLocked()) {
                challenge.setConsumed(true);
                repository.save(challenge);
                throw new TooManyAttemptsException("Too many incorrect attempts — request a new OTP");
            }
            repository.save(challenge);
            throw new InvalidOtpException("Incorrect code", remaining);
        }

        // Success: consume the OTP so it can never be reused.
        challenge.setConsumed(true);
        repository.save(challenge);
        log.info("OTP verified for '{}' — issuing tokens", challenge.getIdentifier());

        return tokenIssuer.issueFor(challenge.getIdentifier());
    }

    /** Rejects a new OTP request that arrives before the resend cooldown elapses. */
    private void enforceResendCooldown(OtpRequest request) {
        repository.findTopByIdentifierAndPurposeOrderByCreatedAtDesc(
                        request.getIdentifier(), request.getPurpose())
                .ifPresent(last -> {
                    LocalDateTime nextAllowed =
                            last.getCreatedAt().plusSeconds(properties.getResendCooldownSeconds());
                    if (LocalDateTime.now().isBefore(nextAllowed)) {
                        throw new TooManyAttemptsException(
                                "An OTP was just requested — please wait before requesting another");
                    }
                });
    }

    /** Masks an identifier for user-facing messages: ada@x.io → a***@x.io. */
    private String mask(String identifier) {
        int at = identifier.indexOf('@');
        if (at > 1) {
            return identifier.charAt(0) + "***" + identifier.substring(at);
        }
        if (identifier.length() > 4) {
            return "***" + identifier.substring(identifier.length() - 4);
        }
        return "***";
    }
}