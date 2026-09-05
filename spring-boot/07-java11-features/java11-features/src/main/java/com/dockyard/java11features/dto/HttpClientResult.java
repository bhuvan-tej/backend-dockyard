package com.dockyard.java11features.dto;

import lombok.Builder;

/**
 * HttpClientResult — the outcome of a request made with
 * {@link java.net.http.HttpClient} (standardized in Java 11), against a
 * tiny embedded HTTP server started in-process so this demo never depends
 * on external network access.
 */
@Builder
public record HttpClientResult(
        String requestUri,
        int statusCode,
        String responseBody,
        long elapsedMillis
) { }