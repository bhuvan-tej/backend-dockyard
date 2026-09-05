package com.dockyard.java8features.dto;

import lombok.Builder;

/**
 * Java8DemoResponse — every demo endpoint in this app returns one of these,
 * on purpose: the RESULT alone doesn't teach anything. Pairing it with the
 * exact {@code codeSnippet} that produced it and a plain-English
 * {@code explanation} turns every API call into a self-contained lesson —
 * you can `curl` an endpoint and understand the Java 8 concept without even
 * opening the source file.
 */
@Builder
public record Java8DemoResponse<T>(
        String operation,
        String description,
        String codeSnippet,
        T result
) { }