package com.dockyard.otpauth.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler — one place that turns every exception into a clean
 * {@link ErrorResponse}. Controllers and services stay free of try/catch noise.
 *
 * Status-code map:
 *   MethodArgumentNotValidException → 400  (bad field values)
 *   InvalidOtpException             → 401  (wrong / already-used code)
 *   InvalidTokenException           → 401  (bad / expired / wrong-type JWT)
 *   ChallengeNotFoundException      → 404  (unknown challengeId)
 *   OtpExpiredException             → 410  (validity window elapsed)
 *   TooManyAttemptsException        → 429  (locked out / resend too soon)
 *   Exception (catch-all)           → 500  (unexpected — logged with stack trace)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Bean Validation failures — collect every field error into a map. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            fieldErrors.put(field, error.getDefaultMessage());
        });

        log.warn("Validation failed for {}: {}", request.getRequestURI(), fieldErrors);
        return build(HttpStatus.BAD_REQUEST, "Validation Failed",
                "Request validation failed", request, fieldErrors);
    }

    /** Wrong or already-consumed OTP. */
    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOtp(
            InvalidOtpException ex, HttpServletRequest request) {

        log.warn("Invalid OTP at {} ({} attempts left)", request.getRequestURI(), ex.getAttemptsRemaining());
        Map<String, String> extra = Map.of("attemptsRemaining", String.valueOf(ex.getAttemptsRemaining()));
        return build(HttpStatus.UNAUTHORIZED, "Invalid OTP", ex.getMessage(), request, extra);
    }

    /** Bad, tampered, expired or wrong-type JWT. */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(
            InvalidTokenException ex, HttpServletRequest request) {

        log.warn("Invalid token at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "Invalid Token", ex.getMessage(), request, null);
    }

    /** Unknown challengeId. */
    @ExceptionHandler(ChallengeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ChallengeNotFoundException ex, HttpServletRequest request) {

        log.warn("Challenge not found at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request, null);
    }

    /** OTP validity window elapsed. */
    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ErrorResponse> handleExpired(
            OtpExpiredException ex, HttpServletRequest request) {

        log.warn("OTP expired at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.GONE, "OTP Expired", ex.getMessage(), request, null);
    }

    /** Attempt limit reached, or OTP re-requested before the cooldown. */
    @ExceptionHandler(TooManyAttemptsException.class)
    public ResponseEntity<ErrorResponse> handleTooMany(
            TooManyAttemptsException ex, HttpServletRequest request) {

        log.warn("Rate limit at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", ex.getMessage(), request, null);
    }

    /** Safety net — nothing unexpected reaches the client raw. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", request, null);
    }

    /** Shared builder so every handler produces an identically shaped response. */
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error,
                                                String message, HttpServletRequest request,
                                                Map<String, String> fieldErrors) {
        ErrorResponse body = ErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(fieldErrors)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}