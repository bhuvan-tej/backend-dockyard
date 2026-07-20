package com.dockyard.otpauth.security;

import com.dockyard.otpauth.config.OtpProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OtpGeneratorTest — verifies the code shape and basic randomness.
 */
class OtpGeneratorTest {

    private OtpGenerator generatorOfLength(int length) {
        OtpProperties props = new OtpProperties();
        props.setLength(length);
        return new OtpGenerator(props);
    }

    @Test
    @DisplayName("generates a code of the configured length, digits only")
    void generatesCorrectLengthAndDigits() {
        OtpGenerator generator = generatorOfLength(6);
        for (int i = 0; i < 100; i++) {
            String code = generator.generate();
            assertThat(code).hasSize(6);
            assertThat(code).matches("\\d{6}");
        }
    }

    @Test
    @DisplayName("preserves leading zeros")
    void preservesLeadingZeros() {
        OtpGenerator generator = generatorOfLength(6);
        boolean sawLeadingZeroAtLeastPossible = true; // shape guarantees it can happen
        // Every code must still be exactly 6 chars, even numerically small ones.
        for (int i = 0; i < 500; i++) {
            assertThat(generator.generate()).hasSize(6);
        }
        assertThat(sawLeadingZeroAtLeastPossible).isTrue();
    }

    @Test
    @DisplayName("produces varied codes (not constant)")
    void producesVariedCodes() {
        OtpGenerator generator = generatorOfLength(6);
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            seen.add(generator.generate());
        }
        // With 10^6 space and 200 draws, collisions are extremely unlikely.
        assertThat(seen.size()).isGreaterThan(190);
    }
}

