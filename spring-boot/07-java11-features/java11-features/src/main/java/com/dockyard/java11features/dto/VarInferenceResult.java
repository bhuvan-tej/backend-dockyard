package com.dockyard.java11features.dto;

import lombok.Builder;

/**
 * VarInferenceResult — shows what {@code var} actually resolves to at
 * compile time for a given declaration. {@code var} is NOT dynamic/weak
 * typing — the compiler still infers and bakes in a single concrete static
 * type at compile time from the initializer expression; {@code var} only
 * removes the need to WRITE that type out.
 */
@Builder
public record VarInferenceResult(
        String declaration,
        String inferredType,
        String value
) { }