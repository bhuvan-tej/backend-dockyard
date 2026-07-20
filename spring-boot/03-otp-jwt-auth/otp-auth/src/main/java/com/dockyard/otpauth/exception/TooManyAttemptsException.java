package com.dockyard.otpauth.exception;

/**
 * TooManyAttemptsException → 429.
 * The challenge is locked because the attempt limit was reached, or an OTP was
 * re-requested before the resend cooldown elapsed.
 */
public class TooManyAttemptsException extends RuntimeException {
    public TooManyAttemptsException(String message) {
        super(message);
    }
}