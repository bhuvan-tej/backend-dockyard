package com.dockyard.java17features.dto;

import lombok.Builder;

/**
 * TeeingResult — the output of {@code Collectors.teeing(...)} (Java 12),
 * which runs TWO downstream collectors over a SINGLE pass of the stream and
 * merges their results. Without it you'd have to collect the stream to a
 * list first and traverse it twice.
 */
@Builder
public record TeeingResult(
        long count,
        double sum,
        double average,
        String singlePassNote
) { }

