package com.dockyard.otpauth.exception;

/**
 * ChallengeNotFoundException → 404.
 * The supplied challengeId does not exist (wrong id, or already purged).
 */
public class ChallengeNotFoundException extends RuntimeException {
    public ChallengeNotFoundException(String message) {
        super(message);
    }
}