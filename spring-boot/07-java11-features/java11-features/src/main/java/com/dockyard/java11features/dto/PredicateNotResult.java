package com.dockyard.java11features.dto;

import lombok.Builder;

/**
 * PredicateNotResult — {@code Predicate.not(predicate)} (a static factory
 * added in Java 11) vs the pre-existing instance method
 * {@code predicate.negate()}. Both produce an equivalent negated predicate;
 * {@code not()} exists mainly so a METHOD REFERENCE can be negated directly,
 * without first assigning it to a {@code Predicate} variable just to call
 * {@code .negate()} on it.
 */
@Builder
public record PredicateNotResult(
        String input,
        boolean viaNegate,
        boolean viaStaticNot,
        String note
) { }