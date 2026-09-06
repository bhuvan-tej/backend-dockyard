package com.dockyard.java17features.dto;

import lombok.Builder;

import java.util.List;

/**
 * RecordSummary — what the compiler actually GENERATED for a record, read
 * back at runtime via reflection ({@code Class.getRecordComponents()}), so
 * the "the compiler writes it for you" claim is demonstrated rather than
 * asserted.
 */
@Builder
public record RecordSummary(
        String recordClass,
        List<String> components,
        List<String> generatedMembers,
        String toStringValue,
        boolean equalsByValue,
        boolean hashCodesMatch,
        boolean isFinal,
        String superclass
) { }