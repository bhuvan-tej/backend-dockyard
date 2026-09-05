package com.dockyard.java8features.controller;

import com.dockyard.java8features.dto.Java8DemoResponse;
import com.dockyard.java8features.dto.OptionalPipelineResult;
import com.dockyard.java8features.dto.OrElseVariantResult;
import com.dockyard.java8features.service.OptionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * OptionalController — {@link java.util.Optional} as an explicit alternative
 * to returning {@code null}. See {@link OptionalService} for the detailed "why".
 */
@RestController
@RequestMapping("/java8/optional")
@RequiredArgsConstructor
@Validated
public class OptionalController {

    private final OptionalService service;

    @GetMapping("/creation-variants")
    public Java8DemoResponse<String> creationVariants(
            @RequestParam(required = false) String value) {
        return Java8DemoResponse.<String>builder()
                .operation("Optional.of vs Optional.ofNullable vs Optional.empty")
                .description("Optional.of(x) throws NullPointerException IMMEDIATELY if x is null (fail fast, at creation) — it asserts 'this value is definitely non-null.' Optional.ofNullable(x) is the safe, general-purpose choice when x might legitimately be null. Omit the 'value' query param to see the null case.")
                .codeSnippet("Optional.of(x);        // throws NPE right away if x == null\nOptional.ofNullable(x); // becomes Optional.empty() if x == null")
                .result(service.creationVariants(value))
                .build();
    }

    @GetMapping("/map-filter-pipeline")
    public Java8DemoResponse<OptionalPipelineResult> mapFilterPipeline(
            @RequestParam(required = false, defaultValue = "  hello  ") String value) {
        return Java8DemoResponse.<OptionalPipelineResult>builder()
                .operation(".map().filter().map() pipeline")
                .description("Each step only runs if the Optional is still present going into it — a chain short-circuits the moment any step produces empty, and every remaining step is silently skipped. Try an empty/blank value to see steps get skipped.")
                .codeSnippet("optional.map(String::trim).filter(s -> !s.isEmpty()).map(String::toUpperCase);")
                .result(service.mapFilterPipeline(value))
                .build();
    }

    @GetMapping("/or-else-vs-or-else-get")
    public Java8DemoResponse<OrElseVariantResult> orElseVsOrElseGet(
            @RequestParam(required = false) String value) {
        return Java8DemoResponse.<OrElseVariantResult>builder()
                .operation("orElse(x) vs orElseGet(supplier) — eager vs lazy fallback evaluation")
                .description("orElse(x) evaluates 'x' as a normal Java method argument, EAGERLY, every single call — even when the Optional IS present and 'x' is thrown away. orElseGet(supplier) only invokes the supplier when the Optional turns out to be empty. Omit 'value' to make the Optional empty and see both side effects run.")
                .codeSnippet("optional.orElse(expensiveFallback());       // expensiveFallback() ALWAYS runs\noptional.orElseGet(() -> expensiveFallback()); // only runs if optional is empty")
                .result(service.orElseVsOrElseGet(value))
                .build();
    }

    @GetMapping("/or-else-throw")
    public Java8DemoResponse<String> orElseThrow(
            @RequestParam(required = false) String value) {
        return Java8DemoResponse.<String>builder()
                .operation("orElseThrow(exceptionSupplier) — a custom exception instead of NoSuchElementException")
                .description("Like Optional.get(), but you control exactly which exception (and message) gets thrown when the Optional is empty, instead of the generic NoSuchElementException that plain .get() throws.")
                .codeSnippet("optional.orElseThrow(() -> new NoSuchElementException(\"No value for: \" + input));")
                .result(service.orElseThrowDemo(value))
                .build();
    }

    @GetMapping("/if-present")
    public Java8DemoResponse<String> ifPresent(
            @RequestParam(required = false) String value) {
        return Java8DemoResponse.<String>builder()
                .operation("ifPresent(Consumer) — run code only when a value exists")
                .description("Runs the consumer only if the Optional is present; does absolutely nothing (no exception, no side effect) if it's empty — the null-safe alternative to 'if (x != null) { ... }'.")
                .codeSnippet("optional.ifPresent(v -> log.append(\"got: \" + v));")
                .result(service.ifPresentDemo(value))
                .build();
    }

}