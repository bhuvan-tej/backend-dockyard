package com.dockyard.java8features.service;

import com.dockyard.java8features.dto.OptionalPipelineResult;
import com.dockyard.java8features.dto.OrElseVariantResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * OptionalService — {@link Optional} is an explicit, type-level way to say
 * "this value might not be there," replacing the old convention of returning
 * (or forgetting to check for) {@code null}. It is NOT a general-purpose
 * "maybe" type for fields or method parameters — its intended use is almost
 * exclusively as a METHOD RETURN TYPE.
 *
 * <h2>Creating one</h2>
 * <ul>
 *   <li>{@code Optional.of(value)} — value must be non-null, or it throws
 *       {@code NullPointerException} IMMEDIATELY (fail fast, at creation).</li>
 *   <li>{@code Optional.ofNullable(value)} — wraps a possibly-null value;
 *       becomes {@code Optional.empty()} if it was null.</li>
 *   <li>{@code Optional.empty()} — explicitly "nothing here."</li>
 * </ul>
 *
 * <h2>The classic gotcha: orElse vs orElseGet</h2>
 * {@code orElse(x)} evaluates {@code x} EAGERLY, every single call, whether
 * or not the Optional is present — if {@code x} is an expensive call (a DB
 * query, a network call), you pay that cost even when it's thrown away.
 * {@code orElseGet(supplier)} evaluates the supplier LAZILY, only when the
 * Optional actually turns out to be empty.
 */
@Service
public class OptionalService {

    /** Optional.of vs ofNullable vs empty, and the NPE risk of Optional.of(null). */
    public String creationVariants(String value) {
        Optional<String> ofNullable = Optional.ofNullable(value); // safe with null

        String ofNullableOutcome = ofNullable.isPresent()
                ? "Optional.ofNullable(\"" + value + "\") -> present, value=\"" + ofNullable.get() + "\""
                : "Optional.ofNullable(null) -> Optional.empty()";

        String ofOutcome;
        try {
            Optional<String> of = Optional.of(value); // throws immediately if value is null
            ofOutcome = "Optional.of(\"" + value + "\") -> present, value=\"" + of.get() + "\"";
        } catch (NullPointerException e) {
            ofOutcome = "Optional.of(null) -> threw NullPointerException IMMEDIATELY (this is Optional.of's whole point: fail fast instead of silently wrapping null)";
        }

        return ofNullableOutcome + " | " + ofOutcome + " | Optional.empty() -> " + Optional.empty();
    }

    /** A realistic .map().filter() pipeline, with a step-by-step trace. */
    public OptionalPipelineResult mapFilterPipeline(String value) {
        List<String> steps = new ArrayList<>();
        Optional<String> start = Optional.ofNullable(value);
        steps.add("Optional.ofNullable(\"" + value + "\") -> " + (start.isPresent() ? "present" : "empty"));

        Optional<String> trimmed = start.map(s -> {
            steps.add("map(String::trim) ran");
            return s.trim();
        });

        Optional<String> filtered = trimmed.filter(s -> {
            steps.add("filter(s -> !s.isEmpty()) ran, checking: \"" + s + "\"");
            return !s.isEmpty();
        });

        Optional<String> upper = filtered.map(s -> {
            steps.add("map(String::toUpperCase) ran");
            return s.toUpperCase();
        });

        return OptionalPipelineResult.builder()
                .steps(steps)
                .finalValue(upper.orElse("<empty — a prior step filtered it out or the input was blank/null>"))
                .build();
    }

    /** The eager-vs-lazy gotcha: orElse always runs its argument; orElseGet only runs on empty. */
    public OrElseVariantResult orElseVsOrElseGet(String value) {
        Optional<String> optional = Optional.ofNullable(value);

        boolean[] orElseSideEffectRan = {false};
        boolean[] orElseGetSideEffectRan = {false};

        // NOTE: this "expensive()" call is evaluated eagerly as a Java method
        // argument BEFORE orElse() even runs — that's the whole point of the demo.
        String orElseResult = optional.orElse(expensiveFallback(orElseSideEffectRan));
        String orElseGetResult = optional.orElseGet(() -> expensiveFallback(orElseGetSideEffectRan));

        return OrElseVariantResult.builder()
                .optionalWasPresent(optional.isPresent())
                .orElseValue(orElseResult)
                .orElseSideEffectRan(orElseSideEffectRan[0])
                .orElseGetValue(orElseGetResult)
                .orElseGetSideEffectRan(orElseGetSideEffectRan[0])
                .build();
    }

    private String expensiveFallback(boolean[] ranFlag) {
        ranFlag[0] = true; // proves this code actually executed
        return "computed-fallback";
    }

    /** orElseThrow() — like Optional.get(), but with a custom, meaningful exception instead of NoSuchElementException. */
    public String orElseThrowDemo(String value) {
        Optional<String> optional = Optional.ofNullable(value);
        return optional.orElseThrow(() ->
                new NoSuchElementException("No value present for input: \"" + value + "\" — this is a CUSTOM exception, unlike the generic one Optional.get() throws"));
    }

    /** ifPresent(Consumer) — run code ONLY if a value is present; does nothing (no exception) if empty. */
    public String ifPresentDemo(String value) {
        Optional<String> optional = Optional.ofNullable(value);
        StringBuilder log = new StringBuilder();
        optional.ifPresent(v -> log.append("ifPresent ran with value: \"").append(v).append("\""));
        if (log.isEmpty()) {
            log.append("ifPresent did NOT run — the Optional was empty, and no exception was thrown either");
        }
        return log.toString();
    }

}