package com.dockyard.javastreams.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * StreamCreationService — every way Java 8 gives you to OBTAIN a
 * {@link Stream} in the first place, before any filtering/mapping happens.
 *
 * A Stream is NOT a data structure — it holds no elements of its own. It is a
 * recipe for producing a sequence of values on demand, from some SOURCE.
 * That source can be a collection already in memory, a fixed list of
 * literals, an array, a numeric range, or even values computed lazily,
 * potentially forever (which is why {@code generate}/{@code iterate} below
 * are always paired with {@code limit} — an infinite stream that is never
 * bounded will simply never finish).
 */
@Service
public class StreamCreationService {

    /** The most common source: any existing {@link java.util.Collection}. */
    public List<String> fromCollection(List<String> source) {
        return source.stream()
                .map(String::toUpperCase)
                .toList();
    }

    /** A fixed, inline sequence of values — no collection needed. */
    public List<String> fromVarargs() {
        return Stream.of("stream", "map", "filter", "reduce", "collect")
                .map(String::toUpperCase)
                .toList();
    }

    /** Any array, primitive-boxed or object, can be streamed directly. */
    public List<Integer> fromArray() {
        int[] primitives = {5, 3, 8, 1, 9, 2};
        return Arrays.stream(primitives)
                .boxed() // IntStream -> Stream<Integer>, so it collects into a List<Integer>
                .sorted()
                .toList();
    }

    /**
     * IntStream.range/rangeClosed — the idiomatic Java 8 replacement for a
     * classic {@code for (int i = start; i < end; i++)} loop. {@code range}
     * excludes the upper bound; {@code rangeClosed} includes it.
     */
    public List<Integer> fromIntRange(int startInclusive, int endExclusive) {
        return IntStream.range(startInclusive, endExclusive)
                .boxed()
                .toList();
    }

    /**
     * Stream.generate(Supplier) — an INFINITE stream where every element is
     * produced independently by the supplier (no relationship between one
     * element and the next). MUST be bounded with {@code limit}, or
     * evaluating it never terminates.
     */
    public List<Integer> fromGenerate(int count) {
        int[] counter = {0}; // a plain array used as a mutable box for the lambda below
        return Stream.generate(() -> counter[0]++)
                .limit(count)
                .toList();
    }

    /**
     * Stream.iterate(seed, UnaryOperator) — an INFINITE stream where each
     * element is derived from the PREVIOUS one via the given function,
     * starting from {@code seed}. Also must be bounded with {@code limit}.
     * (Java 9 added a 3-argument overload with a built-in predicate — not
     * used here, to stay strictly within the Java 8 API.)
     */
    public List<Integer> fromIterate(int seed, int count) {
        return Stream.iterate(seed, n -> n * 2)
                .limit(count)
                .toList();
    }

    /** The empty stream — valid, terminates immediately, useful as a base case. */
    public List<String> empty() {
        return Stream.<String>empty()
                .toList();
    }

    /** Stream.concat — glues two streams together into one, source-order preserved. */
    public List<String> concatenated() {
        Stream<String> first = Stream.of("a", "b", "c");
        Stream<String> second = Stream.of("x", "y", "z");
        return Stream.concat(first, second)
                .toList();
    }
}