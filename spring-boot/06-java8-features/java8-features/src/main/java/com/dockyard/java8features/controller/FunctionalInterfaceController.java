package com.dockyard.java8features.controller;

import com.dockyard.java8features.dto.FunctionChainResult;
import com.dockyard.java8features.dto.Java8DemoResponse;
import com.dockyard.java8features.dto.PredicateChainResult;
import com.dockyard.java8features.service.FunctionalInterfaceService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * FunctionalInterfaceController — the {@code java.util.function} family and a
 * hand-rolled custom functional interface. See
 * {@link FunctionalInterfaceService} for the detailed "why".
 */
@RestController
@RequestMapping("/java8/functional-interfaces")
@RequiredArgsConstructor
@Validated
public class FunctionalInterfaceController {

    private final FunctionalInterfaceService service;

    @GetMapping("/predicate")
    public Java8DemoResponse<PredicateChainResult> predicate(
            @RequestParam(defaultValue = "4") int value) {
        return Java8DemoResponse.<PredicateChainResult>builder()
                .operation("Predicate<T>: boolean test(T t), composed with and/or/negate")
                .description("and/or/negate each return a NEW Predicate — the original predicates are never mutated, so they remain independently reusable.")
                .codeSnippet("isEven.and(isPositive).test(value); isEven.or(isPositive).test(value); isEven.negate().test(value);")
                .result(service.predicateComposition(value))
                .build();
    }

    @GetMapping("/function")
    public Java8DemoResponse<FunctionChainResult> function(
            @RequestParam(defaultValue = "hello") @NotBlank String input) {
        return Java8DemoResponse.<FunctionChainResult>builder()
                .operation("Function<T,R>: R apply(T t), composed with andThen/compose")
                .description("andThen runs THIS function first, then feeds the result into the argument. compose runs the ARGUMENT first, then feeds the result into THIS one — same two functions, reversed order. Demonstrated here on n = input.length().")
                .codeSnippet("increment.andThen(doubleIt).apply(n) == (n + 1) * 2;  increment.compose(doubleIt).apply(n) == (n * 2) + 1;")
                .result(service.functionComposition(input))
                .build();
    }

    @GetMapping("/supplier")
    public Java8DemoResponse<String> supplier() {
        return Java8DemoResponse.<String>builder()
                .operation("Supplier<T>: T get() — lazy, on-demand value production")
                .description("The supplier's body does NOT run when it's declared/assigned — only when .get() is actually invoked. Useful for deferring expensive work until (and unless) it's really needed.")
                .codeSnippet("Supplier<String> expensiveValue = () -> computeSomethingExpensive(); expensiveValue.get(); // runs HERE, not before")
                .result(service.supplierLazyGeneration())
                .build();
    }

    @GetMapping("/consumer")
    public Java8DemoResponse<String> consumer(
            @RequestParam(defaultValue = "hello") @NotBlank String text) {
        return Java8DemoResponse.<String>builder()
                .operation("Consumer<T>: void accept(T t), chained with andThen")
                .description("Consumer.andThen runs both consumers, in order, against the SAME input value — useful for 'do this AND that' without writing a bespoke combined lambda.")
                .codeSnippet("logLength.andThen(logUpper).accept(text); // both run, against the same 'text'")
                .result(service.consumerChaining(text))
                .build();
    }

    @GetMapping("/bifunction")
    public Java8DemoResponse<Integer> biFunction(
            @RequestParam(defaultValue = "3") int a,
            @RequestParam(defaultValue = "4") int b) {
        return Java8DemoResponse.<Integer>builder()
                .operation("BiFunction<T,U,R>: R apply(T t, U u) — a Function that takes two arguments")
                .description("Same idea as Function, but for operations that naturally need two inputs, e.g. combining two values into one result.")
                .codeSnippet("BiFunction<Integer, Integer, Integer> multiply = (x, y) -> x * y;")
                .result(service.biFunctionDemo(a, b))
                .build();
    }

    @GetMapping("/custom")
    public Java8DemoResponse<String> custom(
            @RequestParam(defaultValue = "3") int a,
            @RequestParam(defaultValue = "4") int b) {
        return Java8DemoResponse.<String>builder()
                .operation("A hand-rolled @FunctionalInterface — Calculator")
                .description("Any interface with exactly one abstract method is a functional interface, JDK-provided or not — the compiler enforces the 'single abstract method' rule and any matching lambda can implement it with zero 'implements' boilerplate.")
                .codeSnippet("@FunctionalInterface interface Calculator { int calculate(int a, int b); default Calculator andThenDouble() {...} static Calculator addition() {...} }")
                .result(service.customFunctionalInterface(a, b))
                .build();
    }

}