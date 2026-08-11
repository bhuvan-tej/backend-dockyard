package com.dockyard.javastreams.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

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
}