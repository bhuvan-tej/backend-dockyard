package com.dockyard.otpauth.security;

import com.dockyard.otpauth.config.OtpProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * OtpGenerator — produces the numeric one-time code.
 *
 * WHY ISOLATE THIS?
 *   Same idea as the QR project's QrCodeEngine: the "how we make the secret"
 *   detail lives in exactly one place. Swapping to alphanumeric codes or a TOTP
 *   scheme later touches only this class.
 *
 * WHY SecureRandom (not Math.random / Random)?
 *   An OTP is a security token. java.util.Random is a predictable linear
 *   congruential generator — given a few outputs an attacker can predict the
 *   next. SecureRandom is a cryptographically strong PRNG, which is mandatory
 *   for anything guarding access.
 */
@Component
@RequiredArgsConstructor
public class OtpGenerator {

    private final OtpProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a zero-padded numeric code of the configured length.
     * e.g. length 6 → "042973" (leading zeros preserved).
     */
    public String generate() {
        int length = properties.getLength();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(secureRandom.nextInt(10)); // 0–9, uniform
        }
        return sb.toString();
    }
}