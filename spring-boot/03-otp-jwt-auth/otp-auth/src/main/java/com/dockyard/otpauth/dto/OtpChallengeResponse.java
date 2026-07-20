package com.dockyard.otpauth.dto;

import com.dockyard.otpauth.domain.DeliveryChannel;
import com.dockyard.otpauth.domain.OtpPurpose;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * OtpChallengeResponse — returned after an OTP is generated.
 *
 * It contains everything the client needs to drive the verify step and show a
 * countdown — but NOT the code itself, except in dev mode ({@code devCode}),
 * which stands in for the SMS/email that a real deployment would send.
 */
@Data
@Builder
public class OtpChallengeResponse {

    /** Opaque handle to pass back to /otp/verify. */
    private String challengeId;

    private String identifier;
    private OtpPurpose purpose;
    private DeliveryChannel channel;

    /** The exact instant the OTP stops being valid. */
    private LocalDateTime expiresAt;

    /** Convenience: seconds of validity remaining, for a countdown timer. */
    private long ttlSeconds;

    /** How many wrong guesses are allowed before lockout. */
    private int maxAttempts;

    /** Earliest number of seconds after which a new OTP may be requested. */
    private long resendAfterSeconds;

    /** Human-readable note (e.g. "OTP sent to e***@example.com"). */
    private String message;

    /**
     * DEV ONLY. The plain OTP, present only when {@code otp.expose-code=true}.
     * Lets you test end-to-end without an SMS/email gateway. Null in production.
     */
    private String devCode;
}