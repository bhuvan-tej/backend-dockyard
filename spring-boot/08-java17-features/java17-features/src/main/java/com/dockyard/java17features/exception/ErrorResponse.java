package com.dockyard.java17features.exception;

import lombok.Builder;

import java.time.Instant;

/** ErrorResponse — a consistent JSON error shape for every endpoint in this app. */
@Builder
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) { }

