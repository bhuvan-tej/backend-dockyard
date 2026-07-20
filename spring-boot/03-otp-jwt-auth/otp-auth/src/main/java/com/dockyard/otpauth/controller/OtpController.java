package com.dockyard.otpauth.controller;

import com.dockyard.otpauth.dto.OtpChallengeResponse;
import com.dockyard.otpauth.dto.OtpRequest;
import com.dockyard.otpauth.dto.OtpVerifyRequest;
import com.dockyard.otpauth.dto.TokenResponse;
import com.dockyard.otpauth.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OtpController — the passwordless entry point.
 *
 * Full base path is /api/otp (context-path /api + this mapping). Both endpoints
 * are PUBLIC: you cannot present a token when you are still trying to obtain one.
 *
 * The controller stays thin — validate, delegate to the service, shape the HTTP
 * response. No business logic, no JWT, no persistence here.
 */
@Slf4j
@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
@Tag(name = "OTP", description = "Request and verify one-time passwords")
public class OtpController {

    private final OtpService otpService;

    /**
     * POST /api/otp/request
     * Generates a one-time password and (in dev mode) returns it.
     */
    @PostMapping("/request")
    @Operation(summary = "Request an OTP",
            description = "Generates a short-lived OTP for the identifier. In dev mode the code is returned in 'devCode'.")
    public ResponseEntity<OtpChallengeResponse> request(@Valid @RequestBody OtpRequest request) {
        log.info("POST /otp/request identifier={} purpose={}", request.getIdentifier(), request.getPurpose());
        return ResponseEntity.ok(otpService.requestOtp(request));
    }

    /**
     * POST /api/otp/verify
     * Verifies the code and, on success, returns a JWT access + refresh pair.
     */
    @PostMapping("/verify")
    @Operation(summary = "Verify an OTP and receive JWT tokens",
            description = "Exchanges a valid, unexpired, single-use OTP for an access token and a refresh token.")
    public ResponseEntity<TokenResponse> verify(@Valid @RequestBody OtpVerifyRequest request) {
        log.info("POST /otp/verify challengeId={}", request.getChallengeId());
        return ResponseEntity.ok(otpService.verify(request));
    }
}