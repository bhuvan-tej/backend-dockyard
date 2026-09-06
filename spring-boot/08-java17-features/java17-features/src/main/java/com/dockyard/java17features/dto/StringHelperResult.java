package com.dockyard.java17features.dto;

import lombok.Builder;

/** StringHelperResult — one String API addition, its input and its output, side by side with the older idiom. */
@Builder
public record StringHelperResult(
        String method,
        String since,
        String input,
        String output,
        String olderIdiom
) { }

