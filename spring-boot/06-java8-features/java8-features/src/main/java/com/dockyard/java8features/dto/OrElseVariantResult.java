package com.dockyard.java8features.dto;

import lombok.Builder;

/**
 * OrElseVariantResult — demonstrates the single most common {@code Optional}
 * interview gotcha: {@code orElse(x)} evaluates {@code x} EAGERLY every time,
 * even when the Optional is present and {@code x} is never used, whereas
 * {@code orElseGet(supplier)} evaluates the supplier LAZILY, only when
 * actually needed.
 */
@Builder
public record OrElseVariantResult(
        boolean optionalWasPresent,
        String orElseValue,
        boolean orElseSideEffectRan,
        String orElseGetValue,
        boolean orElseGetSideEffectRan
) { }