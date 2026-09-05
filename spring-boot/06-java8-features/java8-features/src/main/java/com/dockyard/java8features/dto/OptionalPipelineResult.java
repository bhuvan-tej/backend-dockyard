package com.dockyard.java8features.dto;

import lombok.Builder;

import java.util.List;

/**
 * OptionalPipelineResult — the step-by-step trace of an
 * {@link java.util.Optional} pipeline, so the reader can see exactly which
 * steps ran (or were skipped, because the Optional was already empty) rather
 * than just the final value.
 */
@Builder
public record OptionalPipelineResult(
        List<String> steps,
        String finalValue
) { }