package com.dockyard.springai.exception;

/**
 * Thrown at the {@code ai/} boundary when the model provider can't be reached or
 * fails. Mapped to HTTP 503 by {@link GlobalExceptionHandler}. Keeps every
 * Spring AI / provider exception from leaking into the rest of the app.
 */
public class AiUnavailableException extends RuntimeException {

    public AiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

