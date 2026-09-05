package com.dockyard.java8features.controller;

import com.dockyard.java8features.dto.Java8DemoResponse;
import com.dockyard.java8features.dto.LambdaCaptureResult;
import com.dockyard.java8features.service.LambdaService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * LambdaController — lambda syntax forms and variable capture.
 * See {@link LambdaService} for the detailed "why".
 */
@RestController
@RequestMapping("/java8/lambda")
@RequiredArgsConstructor
@Validated
public class LambdaController {

    private final LambdaService service;

    @GetMapping("/no-arg")
    public Java8DemoResponse<String> noArg() {
        return Java8DemoResponse.<String>builder()
                .operation("Lambda form: () -> {} — no parameters")
                .description("Runnable's abstract method is void run() — no params, no return value, so the lambda's parameter list is empty parens.")
                .codeSnippet("Runnable r = () -> { /* ... */ }; r.run();")
                .result(service.noArgLambda())
                .build();
    }

    @GetMapping("/single-param")
    public Java8DemoResponse<String> singleParam(
            @RequestParam(defaultValue = "Ada") @NotBlank String name) {
        return Java8DemoResponse.<String>builder()
                .operation("Lambda form: n -> ... — a single parameter, parens optional")
                .description("With exactly one inferred-type parameter, the surrounding parentheses are optional: 'n -> ...' and '(n) -> ...' are identical.")
                .codeSnippet("Consumer<String> greeter = n -> System.out.printf(\"Hello, %s!%n\", n);")
                .result(service.singleParamLambda(name))
                .build();
    }

    @GetMapping("/two-arg/expression-body")
    public Java8DemoResponse<Integer> twoArgExpressionBody(
            @RequestParam(defaultValue = "3") int a,
            @RequestParam(defaultValue = "4") int b) {
        return Java8DemoResponse.<Integer>builder()
                .operation("Lambda form: (a, b) -> a + b — expression body")
                .description("An expression body has no braces, no 'return' keyword, and no trailing semicolon — the expression's value IS the return value.")
                .codeSnippet("BinaryOperator<Integer> sum = (x, y) -> x + y;")
                .result(service.twoArgExpressionLambda(a, b))
                .build();
    }

    @GetMapping("/two-arg/block-body")
    public Java8DemoResponse<Integer> twoArgBlockBody(
            @RequestParam(defaultValue = "3") int a,
            @RequestParam(defaultValue = "4") int b) {
        return Java8DemoResponse.<Integer>builder()
                .operation("Lambda form: (a, b) -> { ...; return ...; } — block body")
                .description("A block body can hold multiple statements, but unlike an expression body it MUST use an explicit 'return' to produce a value.")
                .codeSnippet("BinaryOperator<Integer> sum = (x, y) -> { int total = x + y; return total; };")
                .result(service.twoArgBlockBodyLambda(a, b))
                .build();
    }

    @GetMapping("/effectively-final-capture")
    public Java8DemoResponse<LambdaCaptureResult> effectivelyFinalCapture(
            @RequestParam(defaultValue = "closures") @NotBlank String message) {
        return Java8DemoResponse.<LambdaCaptureResult>builder()
                .operation("Variable capture: 'effectively final' local variables")
                .description("A lambda may read a local variable from its enclosing scope only if it is never reassigned after initialization — the lambda captures the VALUE at creation time, not a live reference. Reassigning it anywhere is a compile error.")
                .codeSnippet("String message = ...; Runnable r = () -> log.add(message); // legal — 'message' is never reassigned")
                .result(service.effectivelyFinalCapture(message))
                .build();
    }

}