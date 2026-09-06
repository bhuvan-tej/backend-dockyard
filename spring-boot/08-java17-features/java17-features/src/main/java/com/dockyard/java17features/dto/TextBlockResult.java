package com.dockyard.java17features.dto;

import lombok.Builder;

/**
 * TextBlockResult — a text block's rendered value plus the numbers that
 * prove what the compiler stripped: incidental indentation is removed at
 * COMPILE time, so {@code length} and {@code lineCount} reflect the real
 * runtime String, not what you see in the source file.
 */
@Builder
public record TextBlockResult(
        String label,
        String value,
        int length,
        long lineCount,
        String note
) { }

