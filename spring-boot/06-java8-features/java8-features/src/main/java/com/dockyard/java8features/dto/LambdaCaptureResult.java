package com.dockyard.java8features.dto;

import lombok.Builder;

import java.util.List;

/**
 * LambdaCaptureResult — demonstrates "effectively final" variable capture: a
 * lambda can read a local variable from its enclosing scope ONLY if that
 * variable is never reassigned after initialization. The lambda captures the
 * VALUE at creation time, not a live reference to the variable itself.
 */
@Builder
public record LambdaCaptureResult(
        String capturedValue,
        List<String> invocationLog,
        String note
) { }