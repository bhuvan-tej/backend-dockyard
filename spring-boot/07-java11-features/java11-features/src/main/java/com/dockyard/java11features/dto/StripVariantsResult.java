package com.dockyard.java11features.dto;

import lombok.Builder;

/**
 * StripVariantsResult — {@code String.strip()}/{@code stripLeading()}/
 * {@code stripTrailing()} are Unicode-aware (they use
 * {@link Character#isWhitespace(char)}), whereas the old {@code trim()} only
 * strips characters {@code <= U+0020}. The two disagree on some Unicode
 * whitespace characters, which is exactly what this comparison surfaces.
 */
@Builder
public record StripVariantsResult(
        String originalWithMarkers,
        String trimResult,
        String stripResult,
        String stripLeadingResult,
        String stripTrailingResult,
        boolean trimAndStripAgree
) { }