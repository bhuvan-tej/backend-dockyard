package com.dockyard.java8features.service;

import com.dockyard.java8features.dto.LambdaCaptureResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

/**
 * LambdaService — the syntax forms a lambda expression can take, and the
 * single most-asked lambda interview question: variable capture.
 *
 * <h2>Syntax forms, from most to least verbose</h2>
 * <pre>
 *   (int a, int b) -> { return a + b; }   // explicit types, block body
 *   (a, b) -> { return a + b; }           // inferred types, block body
 *   (a, b) -> a + b                       // inferred types, expression body (implicit return)
 *   a -> a * 2                            // single param, parens optional
 *   () -> 42                              // no params
 * </pre>
 *
 * <h2>Variable capture — "effectively final"</h2>
 * A lambda may read a local variable from its enclosing scope only if that
 * variable is never reassigned after its initial assignment (it doesn't need
 * the {@code final} keyword, just the property of never changing — hence
 * "effectively final"). The lambda captures a COPY of the value at creation
 * time; it does not close over a live, mutable reference the way some other
 * languages' closures do. This is why you cannot do
 * {@code count++; return () -> count;} — the compiler rejects it because
 * {@code count} is reassigned after the lambda could observe it.
 *
 * <h2>Lambda vs. anonymous inner class</h2>
 * They look similar but differ in real ways: a lambda has no {@code this} of
 * its own (it refers to the ENCLOSING class's {@code this}), cannot declare
 * fields, and is typically compiled with {@code invokedynamic} rather than a
 * synthetic {@code .class} file per lambda — an anonymous class always gets
 * its own compiled class file and its own {@code this}.
 */
@Service
public class LambdaService {

    /** () -> {} — no parameters, a block body, no return value. */
    public String noArgLambda() {
        Runnable r = () -> { /* intentionally does nothing observable */ };
        r.run();
        return "Executed a Runnable lambda with signature () -> {} — zero params, zero return value.";
    }

    /** name -> ... — a single parameter, parens optional, expression body. */
    public String singleParamLambda(String name) {
        Consumer<String> greeter = n -> System.out.printf("Hello, %s!%n", n);
        greeter.accept(name);
        return "Consumer<String> lambda 'n -> ...' accepted \"" + name + "\" (parens around a single inferred-type param are optional).";
    }

    /** (a, b) -> a + b — multiple parameters, expression body, implicit return. */
    public int twoArgExpressionLambda(int a, int b) {
        BinaryOperator<Integer> sum = (x, y) -> x + y; // expression body: no braces, no 'return' keyword, no semicolon
        return sum.apply(a, b);
    }

    /** (a, b) -> { ...; return ...; } — a block body requires explicit 'return'. */
    public int twoArgBlockBodyLambda(int a, int b) {
        BinaryOperator<Integer> sumWithLogging = (x, y) -> {
            int total = x + y; // a block body can hold multiple statements
            return total;      // and MUST use an explicit 'return', unlike an expression body
        };
        return sumWithLogging.apply(a, b);
    }

    /**
     * Demonstrates that a lambda captures the VALUE of an effectively-final
     * local variable at creation time, not a live reference. Two lambdas
     * created from two different values of the same variable name each keep
     * their own captured copy.
     */
    public LambdaCaptureResult effectivelyFinalCapture(String message) {
        List<String> log = new ArrayList<>();

        // 'message' is a method parameter — never reassigned below, so it's
        // effectively final and legal to capture.
        Runnable printer = () -> log.add("Captured value at creation time: \"" + message + "\"");
        printer.run();

        // A NEW effectively-final local, captured by a SECOND lambda — proves
        // each lambda keeps its own independent copy, not a shared reference.
        String upper = message.toUpperCase();
        Runnable upperPrinter = () -> log.add("A second lambda capturing a DIFFERENT effectively-final local: \"" + upper + "\"");
        upperPrinter.run();

        return LambdaCaptureResult.builder()
                .capturedValue(message)
                .invocationLog(log)
                .note("If 'message' were reassigned anywhere after the lambda is created, this would be a COMPILE ERROR: "
                        + "\"local variables referenced from a lambda expression must be final or effectively final\".")
                .build();
    }

}