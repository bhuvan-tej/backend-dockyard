package com.dockyard.java8features.controller;

import com.dockyard.java8features.dto.Java8DemoResponse;
import com.dockyard.java8features.service.MethodReferenceService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * MethodReferenceController — the four kinds of method reference.
 * See {@link MethodReferenceService} for the detailed "why".
 */
@RestController
@RequestMapping("/java8/method-references")
@RequiredArgsConstructor
@Validated
public class MethodReferenceController {

    private final MethodReferenceService service;

    @GetMapping("/static-method")
    public Java8DemoResponse<Integer> staticMethod(
            @RequestParam(defaultValue = "42") @NotBlank String number) {
        return Java8DemoResponse.<Integer>builder()
                .operation("Kind 1 — static method reference: ClassName::staticMethod")
                .description("Equivalent to the lambda 's -> Integer.parseInt(s)' — the compiler matches it against Function<String,Integer>.")
                .codeSnippet("Function<String, Integer> parser = Integer::parseInt;")
                .result(service.staticMethodReference(number))
                .build();
    }

    @GetMapping("/bound-instance-method")
    public Java8DemoResponse<String> boundInstanceMethod(
            @RequestParam(defaultValue = "world") @NotBlank String text) {
        return Java8DemoResponse.<String>builder()
                .operation("Kind 2 — bound instance method reference: particularObject::instanceMethod")
                .description("The instance ('greeting') already exists and is fixed at the time the reference is created — equivalent to '() -> greeting.toUpperCase()'.")
                .codeSnippet("String greeting = \"Hello, \" + text; Supplier<String> upperCaser = greeting::toUpperCase;")
                .result(service.boundInstanceMethodReference(text))
                .build();
    }

    @GetMapping("/unbound-instance-method")
    public Java8DemoResponse<List<String>> unboundInstanceMethod(
            @RequestParam(defaultValue = "alpha,beta,gamma") List<String> words) {
        return Java8DemoResponse.<List<String>>builder()
                .operation("Kind 3 — unbound instance method reference: ClassName::instanceMethod")
                .description("No specific instance is fixed in advance — the instance to call the method on becomes the FIRST parameter of the functional interface, supplied at call time. Equivalent to 's -> s.toUpperCase()'.")
                .codeSnippet("Function<String, String> upperCaser = String::toUpperCase; words.forEach(w -> upperCaser.apply(w));")
                .result(service.unboundInstanceMethodReference(words))
                .build();
    }

    @GetMapping("/constructor")
    public Java8DemoResponse<List<String>> constructor(
            @RequestParam(defaultValue = "alpha,beta") List<String> seeds) {
        return Java8DemoResponse.<List<String>>builder()
                .operation("Kind 4 — constructor reference: ClassName::new")
                .description("Equivalent to the lambda 's -> new StringBuilder(s)' — works for any constructor whose parameter list matches the functional interface's abstract method.")
                .codeSnippet("Function<String, StringBuilder> builderFactory = StringBuilder::new;")
                .result(service.constructorReference(seeds).stream().map(StringBuilder::toString).toList())
                .build();
    }

    @GetMapping("/two-arg-unbound")
    public Java8DemoResponse<Boolean> twoArgUnbound(
            @RequestParam(defaultValue = "Hello") @NotBlank String a,
            @RequestParam(defaultValue = "HELLO") @NotBlank String b) {
        return Java8DemoResponse.<Boolean>builder()
                .operation("Kind 3, two-argument form: ClassName::instanceMethod as a BiFunction")
                .description("The first parameter supplies the instance ('s1'), the second is the method's own argument ('s2') — equivalent to '(s1, s2) -> s1.equalsIgnoreCase(s2)'.")
                .codeSnippet("BiFunction<String, String, Boolean> equalsIgnoreCase = String::equalsIgnoreCase;")
                .result(service.twoArgUnboundReference(a, b) == 1)
                .build();
    }

}