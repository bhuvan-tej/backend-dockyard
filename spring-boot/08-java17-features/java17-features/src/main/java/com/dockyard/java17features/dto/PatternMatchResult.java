package com.dockyard.java17features.dto;

import lombok.Builder;

/** PatternMatchResult — what a pattern matched, what it bound, and what came out. */
@Builder
public record PatternMatchResult(
        String input,
        String runtimeType,
        String matchedPattern,
        String output
) { }

