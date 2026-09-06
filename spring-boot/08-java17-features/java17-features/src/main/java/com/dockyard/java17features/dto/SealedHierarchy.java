package com.dockyard.java17features.dto;

import lombok.Builder;

import java.util.List;

/** SealedHierarchy — the closed set of permitted subtypes the compiler knows about, read back via reflection. */
@Builder
public record SealedHierarchy(
        String type,
        boolean sealed,
        List<String> permittedSubclasses,
        List<String> subclassModifiers,
        String whyItMatters
) { }