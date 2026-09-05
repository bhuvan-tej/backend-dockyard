package com.dockyard.java11features.controller;

import com.dockyard.java11features.dto.CollectionToArrayResult;
import com.dockyard.java11features.dto.Java11DemoResponse;
import com.dockyard.java11features.dto.PredicateNotResult;
import com.dockyard.java11features.service.FunctionalService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * FunctionalController — Predicate.not, Optional.isEmpty,
 * Collection.toArray(IntFunction). See {@link FunctionalService} for the
 * detailed "why".
 */
@RestController
@RequestMapping("/java11/functional")
@RequiredArgsConstructor
@Validated
public class FunctionalController {

    private final FunctionalService service;

    @GetMapping("/predicate-not")
    public Java11DemoResponse<PredicateNotResult> predicateNot(
            @RequestParam(defaultValue = "   ") String input) {
        return Java11DemoResponse.<PredicateNotResult>builder()
                .operation("Predicate.not(predicate) vs predicate.negate()")
                .description("Both produce an equivalent negated predicate. Predicate.not() matters most for negating a METHOD REFERENCE directly — a bare method reference like String::isBlank has no .negate() to call until it's already assigned to a Predicate variable.")
                .codeSnippet("list.stream().filter(Predicate.not(String::isBlank)); // vs: Predicate<String> p = String::isBlank; list.stream().filter(p.negate());")
                .result(service.predicateNot(input))
                .build();
    }

    @GetMapping("/optional-is-empty")
    public Java11DemoResponse<String> optionalIsEmpty(
            @RequestParam(required = false) String value) {
        return Java11DemoResponse.<String>builder()
                .operation("Optional.isEmpty()")
                .description("The readable inverse of isPresent(), added purely for clarity: 'if (opt.isEmpty())' reads better than 'if (!opt.isPresent())'. Omit the 'value' param to see the empty case.")
                .codeSnippet("Optional<String> opt = Optional.ofNullable(value); if (opt.isEmpty()) { ... }")
                .result(service.optionalIsEmpty(value))
                .build();
    }

    @GetMapping("/collection-to-array")
    public Java11DemoResponse<CollectionToArrayResult> collectionToArray(
            @RequestParam(defaultValue = "alpha,beta,gamma") List<@NotBlank String> items) {
        return Java11DemoResponse.<CollectionToArrayResult>builder()
                .operation("Collection.toArray(IntFunction<T[]> generator)")
                .description("list.toArray(String[]::new) reads clearly as 'give me a String[]' — the IntFunction generator is invoked with the collection's size and produces a correctly-sized, correctly-typed array in one step.")
                .codeSnippet("String[] array = items.toArray(String[]::new);")
                .result(service.collectionToArrayGenerator(items))
                .build();
    }

}