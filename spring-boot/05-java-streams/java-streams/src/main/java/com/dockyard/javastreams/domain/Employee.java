package com.dockyard.javastreams.domain;

/**
 * Employee — the one dataset every demo endpoint in this app operates on.
 * A single, realistic dataset (instead of a different toy example per
 * endpoint) makes it easy to compare operations against each other: you
 * always know what the INPUT looked like, so the effect of each Stream
 * operation on it is obvious.
 */
public record Employee(
        long id,
        String name,
        String department,
        String city,
        int age,
        String gender,
        double salary,
        int joiningYear
) { }