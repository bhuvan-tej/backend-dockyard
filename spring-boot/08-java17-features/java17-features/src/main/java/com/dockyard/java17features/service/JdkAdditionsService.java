package com.dockyard.java17features.service;

import com.dockyard.java17features.dto.CompactNumberResult;
import com.dockyard.java17features.dto.HelpfulNpeResult;
import com.dockyard.java17features.dto.RandomGeneratorResult;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * JdkAdditionsService — the JDK/runtime-level changes from Java 12–17 that
 * you feel in production rather than in the syntax.
 *
 * <ul>
 *   <li><b>Helpful NullPointerExceptions</b> (JEP 358, Java 14; enabled by
 *       DEFAULT from Java 15) — the JVM now names the exact expression that
 *       was null.</li>
 *   <li><b>The RandomGenerator API</b> (JEP 356, Java 17) — pseudo-random
 *       number generation became a pluggable, discoverable SPI instead of
 *       being welded to {@code java.util.Random}.</li>
 *   <li><b>Compact number formatting</b> (Java 12) — locale-aware
 *       "1M"/"1 million" style formatting built into
 *       {@link java.text.NumberFormat}.</li>
 * </ul>
 */
@Service
public class JdkAdditionsService {

    /**
     * Deliberately triggers an NPE so the JVM's own message can be shown.
     *
     * <p>Before Java 14 this told you only "java.lang.NullPointerException"
     * plus a line number — and if the line had several dereferences
     * ({@code a.getB().getC().getD()}) you could not tell WHICH one was
     * null without a debugger or extra logging. The JVM now reconstructs the
     * failing expression from the bytecode and names it.</p>
     */
    public List<HelpfulNpeResult> helpfulNullPointerException() {
        Map<String, String> config = new HashMap<>();
        config.put("present", "value");

        HelpfulNpeResult chained = capture(
                "config.get(\"missing\").toUpperCase()",
                () -> config.get("missing").toUpperCase());

        // A deeper chain — the whole point of JEP 358 is telling you which link broke.
        Map<String, Map<String, String>> nested = new HashMap<>();
        nested.put("outer", new HashMap<>());
        HelpfulNpeResult deep = capture(
                "nested.get(\"outer\").get(\"inner\").length()",
                () -> nested.get("outer").get("inner").length());

        int[] array = new int[3];
        int[][] matrix = new int[2][];
        HelpfulNpeResult arrayAccess = capture(
                "matrix[0][1] = array[0]  (matrix[0] was never allocated)",
                () -> matrix[0][1] = array[0]);

        return List.of(chained, deep, arrayAccess);
    }

    private HelpfulNpeResult capture(String expression, Runnable action) {
        try {
            action.run();
            return HelpfulNpeResult.builder()
                    .expression(expression)
                    .message("(no exception was thrown)")
                    .build();
        } catch (NullPointerException ex) {
            return HelpfulNpeResult.builder()
                    .expression(expression)
                    .message(ex.getMessage())
                    .preJava14Message("java.lang.NullPointerException   (that was the ENTIRE message — no expression, no variable name)")
                    .note("The JVM reconstructs the failing expression from the bytecode. It is computed lazily, only when getMessage() is called, so there is no cost unless an NPE actually happens and is inspected. Controlled by -XX:+ShowCodeDetailsInExceptionMessages, on by default since Java 15.")
                    .build();
        }
    }

    /**
     * The pluggable {@code RandomGenerator} API (JEP 356, Java 17).
     *
     * <p>{@code java.util.Random} hard-wired one 48-bit LCG algorithm from
     * 1995. {@code RandomGenerator} makes the ALGORITHM a runtime choice
     * looked up by name, adds stream-producing methods, and defines
     * sub-interfaces ({@code JumpableGenerator},
     * {@code SplittableGenerator}, ...) for parallel/simulation workloads
     * where you need statistically independent substreams. {@code Random},
     * {@code SplittableRandom} and {@code SecureRandom} were retrofitted to
     * implement it, so old code keeps working.</p>
     */
    public RandomGeneratorResult randomGenerator(String algorithm, long seed, int count) {
        String name = algorithm == null || algorithm.isBlank() ? "L64X128MixRandom" : algorithm.strip();

        // Look the algorithm up by NAME, then create a SEEDED instance so the output
        // is reproducible — RandomGenerator.of(name) would be unseeded and vary per call.
        RandomGeneratorFactory<RandomGenerator> factory;
        try {
            factory = RandomGeneratorFactory.of(name);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown RandomGenerator algorithm '" + name
                    + "'. Available: " + availableAlgorithms(), ex);
        }

        RandomGenerator generator = factory.create(seed);

        return RandomGeneratorResult.builder()
                .algorithm(name)
                .reproducible(true)   // same algorithm + same seed => same sequence, every time
                .values(generator.ints(count, 0, 100).boxed().toList())
                .availableAlgorithms(availableAlgorithms())
                .build();
    }

    private List<String> availableAlgorithms() {
        return RandomGeneratorFactory.all()
                .map(RandomGeneratorFactory::name)
                .sorted()
                .toList();
    }

    /** Compact number formatting (Java 12) — locale-aware "1M" / "1 million". */
    public List<CompactNumberResult> compactNumbers(List<Long> values) {
        List<Long> input = values == null || values.isEmpty()
                ? List.of(999L, 1_500L, 250_000L, 1_000_000L, 3_400_000_000L)
                : values;

        NumberFormat shortFmt = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
        NumberFormat longFmt = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.LONG);
        NumberFormat plainFmt = NumberFormat.getNumberInstance(Locale.US);

        return input.stream()
                .map(v -> CompactNumberResult.builder()
                        .value(v)
                        .shortForm(shortFmt.format(v))
                        .longForm(longFmt.format(v))
                        .plainForm(plainFmt.format(v))
                        .build())
                .toList();
    }

    /** Details worth knowing about these three. */
    public List<String> pitfalls() {
        return List.of(
                "Helpful NPE messages arrived in Java 14 (JEP 358) but were OFF by default until Java 15 — they need -XX:+ShowCodeDetailsInExceptionMessages on 14",
                "The message is computed LAZILY, only when getMessage() is called, so there is no runtime cost unless an NPE is actually inspected",
                "The message can leak variable/field names into logs — worth knowing if you ship stack traces to a third party",
                "The JVM can only describe expressions it can reconstruct from the bytecode; a null passed into a method as a plain parameter is still described only as a parameter",
                "RandomGenerator.of(name) creates an UNSEEDED generator — use RandomGeneratorFactory.of(name).create(seed) when you need reproducibility",
                "No RandomGenerator implementation is cryptographically secure unless it is SecureRandom — do not use L64X128MixRandom for tokens or passwords",
                "java.util.Random is now a RandomGenerator too, so old code needs no migration; the legacy 48-bit LCG algorithm is simply one option among many",
                "Compact number formatting is LOCALE-sensitive by design — always pass an explicit Locale rather than relying on the JVM default"
        );
    }
}