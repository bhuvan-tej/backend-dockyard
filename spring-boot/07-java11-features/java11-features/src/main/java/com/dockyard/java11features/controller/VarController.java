package com.dockyard.java11features.controller;

import com.dockyard.java11features.dto.Java11DemoResponse;
import com.dockyard.java11features.dto.VarInferenceResult;
import com.dockyard.java11features.service.VarService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * VarController — local-variable type inference and its Java 11 extension to
 * lambda parameters. See {@link VarService} for the detailed "why".
 */
@RestController
@RequestMapping("/java11/var")
@RequiredArgsConstructor
@Validated
public class VarController {

    private final VarService service;

    @GetMapping("/local-variable")
    public Java11DemoResponse<List<VarInferenceResult>> localVariable() {
        return Java11DemoResponse.<List<VarInferenceResult>>builder()
                .operation("var — local-variable type inference (Java 10, JEP 286)")
                .description("The compiler infers ONE concrete static type from the initializer at compile time and bakes it into the bytecode — var is not dynamic typing, Object, or 'no type'. It only removes redundant left-hand-side typing.")
                .codeSnippet("var count = 42; var label = \"answer\"; var numbers = List.of(1, 2, 3);")
                .result(service.localVariableInference())
                .build();
    }

    @GetMapping("/enhanced-for-loop")
    public Java11DemoResponse<List<String>> enhancedForLoop() {
        return Java11DemoResponse.<List<String>>builder()
                .operation("var in an enhanced for-loop")
                .description("The element type is inferred from the collection's generic type parameter — for a List<String>, the loop variable is inferred as String.")
                .codeSnippet("var fruits = List.of(\"apple\", \"banana\"); for (var fruit : fruits) { ... }")
                .result(service.enhancedForLoop())
                .build();
    }

    @GetMapping("/lambda-params")
    public Java11DemoResponse<Integer> lambdaParams(
            @RequestParam(defaultValue = "3") int a,
            @RequestParam(defaultValue = "4") int b) {
        return Java11DemoResponse.<Integer>builder()
                .operation("var in lambda parameters (Java 11, JEP 323)")
                .description("Added specifically so lambda parameters could carry annotations (a TYPE_USE annotation needs an explicit type or 'var' to attach to — a bare, type-less lambda parameter has no annotation target at all). Every parameter in the list must use var consistently.")
                .codeSnippet("BiFunction<Integer, Integer, Integer> sum = (@NonNegative var x, @NonNegative var y) -> x + y;")
                .result(service.lambdaParams(a, b))
                .build();
    }

    @GetMapping("/try-with-resources")
    public Java11DemoResponse<String> tryWithResources() {
        return Java11DemoResponse.<String>builder()
                .operation("var as a try-with-resources resource variable")
                .description("var works anywhere a local variable can be declared with an initializer, including a try-with-resources resource — here inferring java.io.BufferedReader.")
                .codeSnippet("try (var reader = new BufferedReader(new StringReader(text))) { ... }")
                .result(service.tryWithResourcesDemo())
                .build();
    }

    @GetMapping("/pitfalls")
    public Java11DemoResponse<List<String>> pitfalls() {
        return Java11DemoResponse.<List<String>>builder()
                .operation("Where var CANNOT be used")
                .description("These are all compile errors, so this endpoint documents them as text rather than executing them — fields, non-lambda method parameters, return types, generic type arguments, uninitialized/null locals with no cast, and untyped lambdas/method references all lack something the compiler needs to infer FROM.")
                .codeSnippet("var field; // fields, method params/returns, generic args, and null-without-cast are all illegal")
                .result(service.pitfalls())
                .build();
    }

}