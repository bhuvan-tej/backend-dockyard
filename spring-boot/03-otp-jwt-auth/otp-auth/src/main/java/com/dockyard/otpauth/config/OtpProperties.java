package com.dockyard.otpauth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OtpProperties — all tunable OTP rules, bound from the {@code otp.*} block in
 * application.yml. Centralising them makes the security posture obvious and
 * changeable without touching code.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "otp")
public class OtpProperties {

    /** Number of digits in the generated code. 6 is the industry norm. */
    private int length = 6;

    /** How long a code stays valid, in seconds. After this it is EXPIRED. */
    private long ttlSeconds = 300;

    /** Maximum wrong guesses before the challenge is locked (anti-brute-force). */
    private int maxAttempts = 5;

    /** Minimum gap between two OTP requests for the same identifier+purpose. */
    private long resendCooldownSeconds = 30;

    /**
     * DEV ONLY. When true, the generated code is returned in the request
     * response so the API is testable with no SMS/email gateway. Set to false
     * (or override via env) for anything resembling production.
     */
    private boolean exposeCode = true;
}