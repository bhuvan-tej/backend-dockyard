package com.dockyard.java8features.dto;

import lombok.Builder;

/**
 * PredicateChainResult — shows {@link java.util.function.Predicate}
 * composition via {@code and}, {@code or}, and {@code negate}, which build a
 * NEW predicate instead of mutating either original one.
 */
@Builder
public record PredicateChainResult(
        int input,
        boolean isEven,
        boolean isPositive,
        boolean isEvenAndPositive,
        boolean isEvenOrPositive,
        boolean isNotEven
) { }