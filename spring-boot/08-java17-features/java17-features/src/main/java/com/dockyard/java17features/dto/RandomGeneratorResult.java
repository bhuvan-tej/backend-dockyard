package com.dockyard.java17features.dto;

import lombok.Builder;

import java.util.List;

/**
 * RandomGeneratorResult — output of the pluggable {@code RandomGenerator}
 * API (JEP 356, Java 17). The point is that the ALGORITHM is now a
 * first-class, swappable choice looked up by name, instead of being
 * hard-wired into {@code java.util.Random}.
 */
@Builder
public record RandomGeneratorResult(
        String algorithm,
        boolean reproducible,
        List<Integer> values,
        List<String> availableAlgorithms
) { }

