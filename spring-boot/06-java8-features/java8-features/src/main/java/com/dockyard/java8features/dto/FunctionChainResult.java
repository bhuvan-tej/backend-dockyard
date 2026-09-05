package com.dockyard.java8features.dto;

import lombok.Builder;

/**
 * FunctionChainResult — {@code andThen} runs THIS function first, then feeds
 * its output into the next one; {@code compose} runs the ARGUMENT first, then
 * feeds its output into this one. Same two functions, reversed order — a
 * classic interview trip-up.
 */
@Builder
public record FunctionChainResult(
        String input,
        int andThenResult,
        int composeResult,
        String explanation
) { }