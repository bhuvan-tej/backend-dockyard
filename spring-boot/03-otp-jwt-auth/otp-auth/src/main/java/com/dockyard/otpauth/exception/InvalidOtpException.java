package com.dockyard.otpauth.exception;

/**
 * InvalidOtpException → 401.
 * The code was wrong (or the challenge was already consumed). Carries how many
 * attempts remain so a UI can warn the user before lockout.
 */
public class InvalidOtpException extends RuntimeException {

    private final int attemptsRemaining;

    public InvalidOtpException(String message, int attemptsRemaining) {
        super(message);
        this.attemptsRemaining = attemptsRemaining;
    }

    public int getAttemptsRemaining() {
        return attemptsRemaining;
    }
}