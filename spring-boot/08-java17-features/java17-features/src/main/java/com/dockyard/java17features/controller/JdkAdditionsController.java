package com.dockyard.java17features.controller;

import com.dockyard.java17features.dto.CompactNumberResult;
import com.dockyard.java17features.dto.HelpfulNpeResult;
import com.dockyard.java17features.dto.Java17DemoResponse;
import com.dockyard.java17features.dto.RandomGeneratorResult;
import com.dockyard.java17features.service.JdkAdditionsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * JdkAdditionsController — the runtime/JDK-level changes from Java 12–17.
 * See {@link JdkAdditionsService} for the detailed "why".
 */
@RestController
@RequestMapping("/java17/jdk")
@RequiredArgsConstructor
@Validated
public class JdkAdditionsController {

    private final JdkAdditionsService service;

    @GetMapping("/helpful-npe")
    public Java17DemoResponse<List<HelpfulNpeResult>> helpfulNpe() {
        return Java17DemoResponse.<List<HelpfulNpeResult>>builder()
                .operation("helpful NullPointerException messages (Java 14, JEP 358 — on by default since 15)")
                .description("Three NPEs are triggered on purpose so you can read the JVM's own message. Pre-Java-14 you got 'java.lang.NullPointerException' and a line number, and on a chained call like a.getB().getC() you could not tell WHICH link was null without a debugger. The JVM now reconstructs the failing expression from the bytecode — lazily, only when getMessage() is called, so there is no cost unless an NPE actually happens.")
                .codeSnippet("config.get(\"missing\").toUpperCase();  // Cannot invoke \"String.toUpperCase()\" because the return value of \"java.util.Map.get(Object)\" is null")
                .result(service.helpfulNullPointerException())
                .build();
    }

    @GetMapping("/random-generator")
    public Java17DemoResponse<RandomGeneratorResult> randomGenerator(
            @RequestParam(defaultValue = "L64X128MixRandom") String algorithm,
            @RequestParam(defaultValue = "42") long seed,
            @RequestParam(defaultValue = "5") @Min(1) @Max(50) int count) {
        return Java17DemoResponse.<RandomGeneratorResult>builder()
                .operation("the pluggable RandomGenerator API (Java 17, JEP 356)")
                .description("java.util.Random hard-wired a single 48-bit LCG from 1995. RandomGenerator turns the ALGORITHM into a runtime choice looked up by name, adds stream-producing methods, and defines sub-interfaces (Jumpable, Splittable, ...) for parallel and simulation workloads that need statistically independent substreams. Random, SplittableRandom and SecureRandom were all retrofitted to implement it, so existing code keeps working. Same algorithm + same seed always gives the same sequence — re-run this endpoint to confirm.")
                .codeSnippet("RandomGeneratorFactory.of(\"L64X128MixRandom\").create(seed).ints(5, 0, 100)")
                .result(service.randomGenerator(algorithm, seed, count))
                .build();
    }

    @GetMapping("/compact-numbers")
    public Java17DemoResponse<List<CompactNumberResult>> compactNumbers() {
        return Java17DemoResponse.<List<CompactNumberResult>>builder()
                .operation("compact number formatting (Java 12)")
                .description("Locale-aware '1M' (SHORT) and '1 million' (LONG) formatting built straight into java.text.NumberFormat — the thing every dashboard used to hand-roll with a chain of if/else and hard-coded suffixes. Always pass an explicit Locale; the output is locale-sensitive by design.")
                .codeSnippet("NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT).format(1_000_000)  // \"1M\"")
                .result(service.compactNumbers(null))
                .build();
    }

    @GetMapping("/pitfalls")
    public Java17DemoResponse<List<String>> pitfalls() {
        return Java17DemoResponse.<List<String>>builder()
                .operation("runtime-addition gotchas")
                .description("When helpful NPE messages are actually enabled, why they can leak field names into logs, why RandomGenerator.of(name) is not reproducible, and why none of the new generators are cryptographically secure.")
                .codeSnippet("RandomGenerator.of(name)            // UNSEEDED — use RandomGeneratorFactory.of(name).create(seed) for reproducibility")
                .result(service.pitfalls())
                .build();
    }
}