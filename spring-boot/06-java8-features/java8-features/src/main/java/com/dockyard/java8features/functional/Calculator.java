package com.dockyard.java8features.functional;

/**
 * Calculator — a hand-rolled {@code @FunctionalInterface}, used to demonstrate
 * that "functional interface" just means "exactly one abstract method" — the
 * compiler enforces it, and any matching lambda or method reference can
 * implement it, with zero {@code implements} boilerplate.
 *
 * {@code @FunctionalInterface} is optional (the compiler infers it from the
 * shape), but it's good practice: it makes the *intent* explicit and gives
 * you a compile error the moment a second abstract method is accidentally
 * added.
 */
@FunctionalInterface
public interface Calculator {

    int calculate(int a, int b);

    // Default and static methods are allowed on a functional interface —
    // they don't count toward the "single abstract method" rule because
    // they already have a body.
    default Calculator andThenDouble() {
        return (a, b) -> calculate(a, b) * 2;
    }

    static Calculator addition() {
        return (a, b) -> a + b;
    }
}