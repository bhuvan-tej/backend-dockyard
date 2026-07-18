package com.dockyard.qrgenerator.engine;

import com.dockyard.qrgenerator.domain.ErrorCorrection;
import com.dockyard.qrgenerator.exception.QrDecodingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * QrCodeEngineTest — verifies the actual ZXing encode/decode behaviour.
 *
 * The most important test in the project: if we can encode text and then
 * decode the very same PNG back to identical text, the whole pipeline works.
 */
class QrCodeEngineTest {

    private final QrCodeEngine engine = new QrCodeEngine();

    @Test
    @DisplayName("encode then decode returns the original text (round-trip)")
    void roundTrip_returnsOriginalContent() {
        String original = "https://github.com/backend-dockyard";

        byte[] png = engine.generatePng(original, 300, 1,
                "#000000", "#FFFFFF", ErrorCorrection.M);
        String decoded = engine.decode(png);

        assertThat(png).isNotEmpty();
        assertThat(decoded).isEqualTo(original);
    }

    @Test
    @DisplayName("round-trip survives custom colours")
    void roundTrip_withCustomColors() {
        String original = "Custom colours still scan";

        byte[] png = engine.generatePng(original, 400, 2,
                "#1A237E", "#E8EAF6", ErrorCorrection.Q);

        assertThat(engine.decode(png)).isEqualTo(original);
    }

    @Test
    @DisplayName("round-trip survives UTF-8 characters")
    void roundTrip_withUnicode() {
        String original = "café ☕ — QR ✓ 日本語";

        byte[] png = engine.generatePng(original, 350, 1,
                "#000000", "#FFFFFF", ErrorCorrection.H);

        assertThat(engine.decode(png)).isEqualTo(original);
    }

    @Test
    @DisplayName("decoding a non-image throws QrDecodingException")
    void decode_nonImage_throws() {
        byte[] notAnImage = "this is plain text, not a PNG".getBytes();

        assertThatThrownBy(() -> engine.decode(notAnImage))
                .isInstanceOf(QrDecodingException.class);
    }
}

