package com.dockyard.javastreams.dto;

import lombok.Builder;

/**
 * StreamDemoResponse — every demo endpoint in this app returns one of these,
 * on purpose: the RESULT alone doesn't teach anything. Pairing it with the
 * exact {@code codeSnippet} that produced it and a plain-English
 * {@code explanation} turns every API call into a self-contained lesson —
 * you can `curl` an endpoint and understand the Stream operation without
 * even opening the source file.
 */
@Builder
public record StreamDemoResponse<T>(
        String operation,
        String description,
        String codeSnippet,
        T result
) { }