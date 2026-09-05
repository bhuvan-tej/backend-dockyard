package com.dockyard.java11features.functional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NonNegative — a {@code TYPE_USE} annotation that exists purely to prove
 * the point of Java 11's JEP 323: a lambda parameter can ONLY be annotated
 * if it's declared with an explicit type OR {@code var} — a bare, type-less
 * lambda parameter (e.g. plain {@code x}) has no annotation target at all.
 * Before Java 11, annotating an inferred lambda parameter meant spelling out
 * its full type just to hang the annotation on it; {@code var} removes that
 * requirement while still allowing the annotation.
 */
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NonNegative {
}