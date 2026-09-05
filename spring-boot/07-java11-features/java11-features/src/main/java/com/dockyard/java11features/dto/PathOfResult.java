package com.dockyard.java11features.dto;

import lombok.Builder;

import java.util.List;

/** PathOfResult — java.nio.file.Path.of(...), added in Java 11 to replace Paths.get(...). */
@Builder
public record PathOfResult(
        String pathOfMultipleSegments,
        String pathOfSingleUri,
        String resolvedAgainstBase,
        List<String> normalizedAfterDotDot,
        boolean isAbsolute
) { }