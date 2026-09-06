package com.dockyard.java17features.service;

import com.dockyard.java17features.dto.StringHelperResult;
import com.dockyard.java17features.dto.TeeingResult;
import com.dockyard.java17features.dto.ToListResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ApiAdditionsService — the smaller library additions from Java 12–17 that
 * show up constantly in day-to-day code (and in interviews).
 *
 * <ul>
 *   <li>{@code Stream.toList()} (Java 16) — the terse terminal operation,
 *       and the UNMODIFIABLE-vs-modifiable difference against
 *       {@code collect(Collectors.toList())}.</li>
 *   <li>{@code Collectors.teeing(c1, c2, merger)} (Java 12) — run TWO
 *       collectors over ONE pass of the stream and merge their results.</li>
 *   <li>{@code String.formatted} (15), {@code String.indent} (12),
 *       {@code String.transform} (12), {@code String.stripIndent} and
 *       {@code String.translateEscapes} (15).</li>
 * </ul>
 */
@Service
public class ApiAdditionsService {

    /**
     * {@code Stream.toList()} vs {@code collect(Collectors.toList())} — the
     * differences that actually matter.
     */
    public ToListResult streamToList() {
        List<String> viaToList = Stream.of("alpha", "beta", "gamma").toList();
        List<String> viaCollectors = Stream.of("alpha", "beta", "gamma").collect(Collectors.toList());

        return ToListResult.builder()
                .viaStreamToList(viaToList)
                .streamToListIsModifiable(isModifiable(viaToList))          // false — UNMODIFIABLE
                .viaCollectorsToList(viaCollectors)
                .collectorsToListIsModifiable(isModifiable(viaCollectors))  // true — an ArrayList in practice
                // The subtle one: toList() is unmodifiable but DOES allow null elements,
                // unlike List.of(...)/List.copyOf(...), which reject them outright.
                .streamToListAllowsNulls(allowsNulls())
                .build();
    }

    private boolean isModifiable(List<String> list) {
        try {
            List<String> copy = list;
            copy.add("mutated");
            copy.remove("mutated");
            return true;
        } catch (UnsupportedOperationException ex) {
            return false;
        }
    }

    private boolean allowsNulls() {
        try {
            List<String> withNull = Stream.of("a", null, "c").toList();
            return withNull.contains(null);
        } catch (NullPointerException ex) {
            return false;
        }
    }

    /**
     * {@code Collectors.teeing} — count and sum in a SINGLE traversal, then
     * merge. Before Java 12 you'd either collect to a list and traverse it
     * twice, or reach for {@code summaryStatistics()} (which only covers the
     * fixed count/sum/min/max/average set — teeing composes ANY two
     * collectors).
     */
    public TeeingResult teeing(List<Double> values) {
        List<Double> input = values == null || values.isEmpty() ? List.of(4.0, 8.0, 15.0, 16.0, 23.0, 42.0) : values;

        TeeingResult result = input.stream().collect(
                Collectors.teeing(
                        Collectors.counting(),                       // downstream #1
                        Collectors.summingDouble(Double::doubleValue), // downstream #2
                        (count, sum) -> TeeingResult.builder()        // merger
                                .count(count)
                                .sum(sum)
                                .average(count == 0 ? 0 : sum / count)
                                .build()));

        return TeeingResult.builder()
                .count(result.count())
                .sum(result.sum())
                .average(result.average())
                .singlePassNote("Both downstream collectors saw the same SINGLE traversal of the stream — the merger only combined their two finished results. A stream can only be consumed once, which is exactly why teeing exists.")
                .build();
    }

    /** A handful of String additions from Java 12–15, each next to the idiom it replaces. */
    public List<StringHelperResult> stringHelpers(String input) {
        String value = input == null || input.isBlank() ? "dockyard" : input;
        List<StringHelperResult> results = new ArrayList<>();

        results.add(StringHelperResult.builder()
                .method("String.formatted(Object...)")
                .since("Java 15")
                .input(value)
                .output("Hello, %s! (%d chars)".formatted(value, value.length()))
                .olderIdiom("String.format(\"Hello, %s!\", value) — same thing, but the template no longer has to come first as an argument")
                .build());

        results.add(StringHelperResult.builder()
                .method("String.indent(int)")
                .since("Java 12")
                .input(value + "\nsecond line")
                .output((value + "\nsecond line").indent(4))
                .olderIdiom("A manual lines().map(l -> \"    \" + l).collect(joining(\"\\n\")) — note indent() also normalises line endings and always appends a trailing \\n")
                .build());

        results.add(StringHelperResult.builder()
                .method("String.transform(Function)")
                .since("Java 12")
                .input(value)
                // transform lets you apply an arbitrary function INLINE in a fluent
                // chain, instead of breaking out to a local variable or a helper call.
                .output(value.transform(s -> s.toUpperCase()).transform(s -> s + "!"))
                .olderIdiom("Nested calls read inside-out: helper2(helper1(value)). transform keeps the chain left-to-right.")
                .build());

        results.add(StringHelperResult.builder()
                .method("String.chars() + Collectors")
                .since("Java 8, shown here with Stream.toList() (16)")
                .input(value)
                .output(value.chars().mapToObj(c -> String.valueOf((char) c)).distinct().toList().toString())
                .olderIdiom(".collect(Collectors.toList()) — toList() is terser, and returns an unmodifiable list")
                .build());

        return results;
    }

    /** Where these additions bite. */
    public List<String> pitfalls() {
        return List.of(
                "Stream.toList() returns an UNMODIFIABLE list — calling add/remove/set on it throws UnsupportedOperationException, unlike collect(Collectors.toList())",
                "Stream.toList() is NOT the same as collect(Collectors.toUnmodifiableList()) either: toList() permits null elements, toUnmodifiableList() throws NullPointerException on them",
                "Collectors.toList() makes NO documented guarantee about the list type or its mutability — relying on it being an ArrayList is a bug waiting to happen",
                "Collectors.teeing exists because a stream can only be consumed ONCE — you cannot just call two terminal operations on the same stream",
                "String.indent(n) always appends a trailing \\n and normalises line terminators — it is not a plain prefix operation",
                "A negative argument to indent() REMOVES up to n leading spaces rather than adding them",
                "Arrays.asList(...) is still fixed-size-mutable, List.of(...) is immutable, and Stream.toList() is unmodifiable-but-null-tolerant — three different contracts people conflate"
        );
    }

    /** Small helper kept for the controller's convenience when parsing a CSV of numbers. */
    public List<Double> parseCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::strip)
                .filter(s -> !s.isBlank())
                .map(Double::parseDouble)
                .toList();
    }
}