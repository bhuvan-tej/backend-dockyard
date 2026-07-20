package com.dockyard.otpauth.exception;

/**
 * InvalidTokenException → 401.
 * A JWT (usually a refresh token) is missing, malformed, tampered with, expired,
 * or of the wrong type. Thrown by the JwtService boundary and the refresh flow.
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}