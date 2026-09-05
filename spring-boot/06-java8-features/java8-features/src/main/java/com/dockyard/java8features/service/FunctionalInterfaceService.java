package com.dockyard.java8features.service;

import com.dockyard.java8features.dto.FunctionChainResult;
import com.dockyard.java8features.dto.PredicateChainResult;
import com.dockyard.java8features.functional.Calculator;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * FunctionalInterfaceService — the {@code java.util.function} family that
 * ships with the JDK, plus a hand-rolled {@code @FunctionalInterface}
 * ({@link Calculator}) proving the concept generalizes to any single-abstract-
 * method interface, not just the built-in ones.
 *
 * <table>
 *   <caption>The four core shapes</caption>
 *   <tr><th>Interface</th><th>Abstract method</th><th>Use for</th></tr>
 *   <tr><td>{@code Predicate<T>}</td><td>{@code boolean test(T t)}</td><td>a yes/no question about a value</td></tr>
 *   <tr><td>{@code Function<T,R>}</td><td>{@code R apply(T t)}</td><td>transform T into R</td></tr>
 *   <tr><td>{@code Supplier<T>}</td><td>{@code T get()}</td><td>lazily produce a value, no input</td></tr>
 *   <tr><td>{@code Consumer<T>}</td><td>{@code void accept(T t)}</td><td>do something with a value, no output</td></tr>
 * </table>
 *
 * All four (and {@code BiFunction}, {@code UnaryOperator}, {@code BinaryOperator})
 * support composition methods that return a NEW function/predicate rather
 * than mutating either operand — {@code Predicate.and/or/negate},
 * {@code Function.andThen/compose}, {@code Consumer.andThen}.
 */
@Service
public class FunctionalInterfaceService {

    /** Predicate<T>: boolean test(T t) — composed with and/or/negate, each returning a NEW predicate. */
    public PredicateChainResult predicateComposition(int value) {
        Predicate<Integer> isEven = n -> n % 2 == 0;
        Predicate<Integer> isPositive = n -> n > 0;

        return PredicateChainResult.builder()
                .input(value)
                .isEven(isEven.test(value))
                .isPositive(isPositive.test(value))
                .isEvenAndPositive(isEven.and(isPositive).test(value))   // both must be true
                .isEvenOrPositive(isEven.or(isPositive).test(value))     // either may be true
                .isNotEven(isEven.negate().test(value))                 // inverts the original — 'isEven' itself is untouched
                .build();
    }

    /**
     * Function<T,R>: R apply(T t) — {@code andThen} runs THIS function first
     * then feeds the next one; {@code compose} runs the ARGUMENT first then
     * feeds it into THIS one. Same two functions, reversed evaluation order —
     * demonstrated here on {@code n = input.length()} so both directions are
     * type-compatible ({@code Function<Integer,Integer>} on both sides).
     */
    public FunctionChainResult functionComposition(String input) {
        int n = input.length();
        Function<Integer, Integer> increment = x -> x + 1;
        Function<Integer, Integer> doubleIt = x -> x * 2;

        int andThenResult = increment.andThen(doubleIt).apply(n); // (n + 1) * 2  — increment runs FIRST
        int composeResult = increment.compose(doubleIt).apply(n); // (n * 2) + 1  — doubleIt runs FIRST

        return FunctionChainResult.builder()
                .input(input)
                .andThenResult(andThenResult)
                .composeResult(composeResult)
                .explanation("andThen: apply THIS function, then feed its result into the argument function. "
                        + "compose: apply the ARGUMENT function first, then feed its result into THIS one. "
                        + "increment.andThen(doubleIt).apply(n) == (n + 1) * 2; increment.compose(doubleIt).apply(n) == (n * 2) + 1.")
                .build();
    }

    /** Supplier<T>: T get() — produces a value lazily, only when get() is actually called. */
    public String supplierLazyGeneration() {
        Supplier<String> expensiveValue = () -> {
            // in real code this might be a DB call, a random UUID, System.currentTimeMillis(), etc.
            return "computed-at-" + System.nanoTime();
        };
        // Nothing above ran any of that work yet — only NOW, on .get(), does it execute.
        return supplierResultOrDefault(expensiveValue);
    }

    private String supplierResultOrDefault(Supplier<String> supplier) {
        return supplier.get(); // the supplier's body runs exactly here, and not a moment before
    }

    /** Consumer<T>: void accept(T t) — andThen chains two consumers to run in sequence on the same input. */
    public String consumerChaining(String text) {
        StringBuilder log = new StringBuilder();
        Consumer<String> logLength = s -> log.append("length=").append(s.length()).append("; ");
        Consumer<String> logUpper = s -> log.append("upper=").append(s.toUpperCase()).append("; ");

        logLength.andThen(logUpper).accept(text); // both run, in this order, against the SAME input
        return log.toString();
    }

    /** BiFunction<T,U,R>: R apply(T t, U u) — like Function but takes two arguments. */
    public int biFunctionDemo(int a, int b) {
        BiFunction<Integer, Integer, Integer> multiply = (x, y) -> x * y;
        return multiply.apply(a, b);
    }

    /** A hand-rolled @FunctionalInterface — Calculator — used exactly like a JDK one. */
    public String customFunctionalInterface(int a, int b) {
        Calculator add = Calculator.addition();       // built via the interface's own static factory method
        Calculator addThenDouble = add.andThenDouble(); // built via the interface's own default method

        return String.format(
                "Calculator.addition().calculate(%d, %d) = %d; .andThenDouble() on top of that = %d",
                a, b, add.calculate(a, b), addThenDouble.calculate(a, b));
    }

}