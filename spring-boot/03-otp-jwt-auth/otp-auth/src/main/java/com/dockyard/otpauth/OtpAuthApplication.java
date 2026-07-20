package com.dockyard.otpauth;

import com.dockyard.otpauth.config.JwtProperties;
import com.dockyard.otpauth.config.OtpProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * OtpAuthApplication — entry point for the OTP + JWT authentication service.
 *
 * WHAT THIS APP DOES:
 *   Generates a one-time password (OTP) for an identifier (email/phone)
 *   Stores only a HASH of the OTP, with a strict validity window (expiry)
 *   Verifies the OTP (single-use, attempt-limited, resend-throttled)
 *   On success, issues a short-lived JWT access token + a long-lived refresh token
 *   Protects endpoints with a stateless JWT filter and refreshes expired access tokens
 *
 * The whole point is demonstrating VALIDITY and EXPIRY done properly: the OTP
 * expires, the access token expires, the refresh token expires — each on its own
 * clock — and every check is enforced server-side.
 */
@SpringBootApplication
@EnableConfigurationProperties({OtpProperties.class, JwtProperties.class})
public class OtpAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(OtpAuthApplication.class, args);
    }

}