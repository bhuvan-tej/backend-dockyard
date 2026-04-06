package com.dockyard.restapicrud.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ErrorResponse — the consistent shape of ALL error responses from this API.
 *
 * WHY A CONSISTENT ERROR SHAPE?
 *
 * Without a standard error format:
 *   404 → Spring's default {"timestamp":...,"status":404,"error":"Not Found","path":...}
 *   400 → A different format with validation details
 *   500 → Yet another format
 *   Client has to handle three different error shapes
 *
 * With a standard format:
 *   ALL errors return the same shape
 *   Client handles errors the same way every time
 *   Error messages are human readable
 *   Validation errors show exactly which field failed and why
 *
 * @JsonInclude(NON_NULL) — omits null fields from the JSON response
 *   errors field is only shown when there are validation errors
 *   not shown for 404 or 500 errors where there are no field errors
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    // HTTP status code — 400, 404, 500 etc
    private int status;

    // Human readable error type — "Bad Request", "Not Found" etc
    private String error;

    // Specific message about what went wrong
    private String message;

    // The URL path that triggered the error
    private String path;

    // When the error occurred
    private LocalDateTime timestamp;

    // Field-level validation errors — only present on 400 Bad Request
    // Map of field name → error message
    // Example: {"name": "must not be blank", "price": "must be positive"}
    private Map<String, String> errors;
}