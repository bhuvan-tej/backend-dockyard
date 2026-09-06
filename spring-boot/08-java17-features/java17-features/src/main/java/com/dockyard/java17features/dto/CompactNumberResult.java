package com.dockyard.java17features.dto;

import lombok.Builder;

/**
 * CompactNumberResult — Java 12's compact number formatting
 * (1_000_000 becomes "1M" in SHORT style, "1 million" in LONG style).
 */
@Builder
public record CompactNumberResult(
        long value,
        String shortForm,
        String longForm,
        String plainForm
) { }
