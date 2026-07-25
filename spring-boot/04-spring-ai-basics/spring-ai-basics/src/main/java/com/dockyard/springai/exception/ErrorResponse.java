package com.dockyard.springai.exception;

import java.time.Instant;
import java.util.Map;

/**
 * A consistent error body for every failed request.
 *
 * @param timestamp when the error happened
 * @param status    HTTP status code
 * @param error     short reason phrase
 * @param message   human-readable detail
 * @param errors    optional field-by-field validation errors (may be null)
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> errors
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(Instant.now(), status, error, message, null);
    }

    public static ErrorResponse validation(int status, String error, Map<String, String> errors) {
        return new ErrorResponse(Instant.now(), status, error, "Validation failed", errors);
    }
}

