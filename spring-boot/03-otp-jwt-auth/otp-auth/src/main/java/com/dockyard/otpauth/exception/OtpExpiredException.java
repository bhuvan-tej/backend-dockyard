package com.dockyard.otpauth.exception;

/**
 * OtpExpiredException → 410 Gone.
 * The code was correct-shaped but its validity window has elapsed. 410 is the
 * most honest status: the resource (this OTP) existed and is now permanently gone.
 */
public class OtpExpiredException extends RuntimeException {
    public OtpExpiredException(String message) {
        super(message);
    }
}