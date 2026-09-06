package com.dockyard.java17features.dto;

import lombok.Builder;

import java.util.List;

/**
 * ToListResult — {@code Stream.toList()} (Java 16) vs
 * {@code collect(Collectors.toList())}. The difference that matters is
 * MUTABILITY: {@code toList()} returns an UNMODIFIABLE list, while
 * {@code Collectors.toList()} makes no such guarantee (in practice an
 * {@code ArrayList}).
 */
@Builder
public record ToListResult(
        List<String> viaStreamToList,
        boolean streamToListIsModifiable,
        List<String> viaCollectorsToList,
        boolean collectorsToListIsModifiable,
        boolean streamToListAllowsNulls
) { }

