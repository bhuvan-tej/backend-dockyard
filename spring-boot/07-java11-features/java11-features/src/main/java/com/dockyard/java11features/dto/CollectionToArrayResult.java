package com.dockyard.java11features.dto;

import lombok.Builder;

/** CollectionToArrayResult — Collection.toArray(IntFunction<T[]>), added in Java 11. */
@Builder
public record CollectionToArrayResult(
        java.util.List<String> sourceCollection,
        String arrayRuntimeType,
        int arrayLength,
        java.util.List<String> arrayContents,
        String note
) { }