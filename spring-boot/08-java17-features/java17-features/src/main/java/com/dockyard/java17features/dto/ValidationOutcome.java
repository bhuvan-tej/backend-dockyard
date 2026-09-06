package com.dockyard.java17features.dto;

import lombok.Builder;

/** ValidationOutcome — whether a record's compact constructor accepted or rejected the given components. */
@Builder
public record ValidationOutcome(
        String attempted,
        boolean accepted,
        String value,
        String rejectionMessage
) { }