package com.dockyard.virtualthreads.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ErrorResponse — the single, consistent error shape for the whole API.
 */
@Data
@Builder
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;

    /** Populated only for validation failures: fieldName → reason. */
    private Map<String, String> errors;
}