package com.dockyard.qrgenerator.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ContentTypeTest — pins down the heuristic classification that powers analytics.
 */
class ContentTypeTest {

    @ParameterizedTest(name = "\"{0}\" is classified as {1}")
    @DisplayName("content is classified into the correct type")
    @CsvSource(delimiter = '|', value = {
            "https://example.com          | URL",
            "http://x.io                  | URL",
            "mailto:a@b.com               | EMAIL",
            "person@company.com           | EMAIL",
            "tel:+9112345                 | PHONE",
            "smsto:+9112345:hi            | SMS",
            "WIFI:T:WPA;S:net;P:pw;;      | WIFI",
            "geo:12.97,77.59              | GEO",
            "BEGIN:VCARD                  | VCARD",
            "just some plain text         | TEXT"
    })
    void classify(String content, ContentType expected) {
        assertThat(ContentType.classify(content.trim())).isEqualTo(expected);
    }

    @Test
    @DisplayName("null or blank content defaults to TEXT")
    void classify_blank_defaultsToText() {
        assertThat(ContentType.classify(null)).isEqualTo(ContentType.TEXT);
        assertThat(ContentType.classify("   ")).isEqualTo(ContentType.TEXT);
    }
}



