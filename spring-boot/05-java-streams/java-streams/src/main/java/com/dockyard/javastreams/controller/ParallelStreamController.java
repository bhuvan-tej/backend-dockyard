package com.dockyard.javastreams.controller;

import com.dockyard.javastreams.dto.StreamDemoResponse;
import com.dockyard.javastreams.service.ParallelStreamService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ParallelStreamController — {@code stream()} vs {@code parallelStream()} on
 * CPU-bound work, plus the classic shared-mutable-state pitfall (see
 * {@link ParallelStreamService} for the detailed contrast with
 * {@code 11-virtual-threads}, which solves the OPPOSITE problem — I/O-bound
 * blocking — rather than this one).
 */
@RestController
@RequestMapping("/streams/parallel")
@RequiredArgsConstructor
@Validated
public class ParallelStreamController {

    private final ParallelStreamService service;

    @Builder
    public record CompareResult(
            int elementCount,
            long sequentialMillis,
            long parallelMillis,
            long resultValue,
            double speedupFactor
    ) {
    }

    @GetMapping("/compare")
    public StreamDemoResponse<CompareResult> compare(
            @RequestParam(defaultValue = "20000") @Min(100) @Max(2_000_000) int elements) {

        var sequential = service.sumOfSquaresSequential(elements);
        var parallel = service.sumOfSquaresParallel(elements);
        double speedup = sequential.elapsedMillis() / (double) Math.max(1, parallel.elapsedMillis());

        CompareResult result = CompareResult.builder()
                .elementCount(elements)
                .sequentialMillis(sequential.elapsedMillis())
                .parallelMillis(parallel.elapsedMillis())
                .resultValue(sequential.resultValue())
                .speedupFactor(Math.round(speedup * 100) / 100.0)
                .build();

        return StreamDemoResponse.<CompareResult>builder()
                .operation("stream() vs parallelStream() — CPU-bound work")
                .description("Runs the IDENTICAL CPU-heavy computation (no I/O, no sleeping) sequentially and in parallel via the JVM's common ForkJoinPool, splitting the range across your CPU cores. Unlike virtual threads (which help I/O-bound blocking), parallel streams help CPU-bound work specifically — try a small 'elements' value and notice the speedup shrinks or disappears: splitting has overhead too.")
                .codeSnippet("IntStream.range(0, " + elements + ").parallel().mapToLong(this::expensiveSquare).sum()")
                .result(result)
                .build();
    }

    @Builder
    public record PitfallResult(
            int requestedCount,
            int unsafeForEachResultSize,
            int safeCollectResultSize,
            boolean dataRaceObserved
    ) {
    }

    @GetMapping("/pitfall")
    public StreamDemoResponse<PitfallResult> pitfall(
            @RequestParam(defaultValue = "50000") @Min(1000) @Max(1_000_000) int count) {

        int unsafeSize = service.unsafeParallelForEachSize(count);
        int safeSize = service.safeParallelCollectSize(count);

        PitfallResult result = PitfallResult.builder()
                .requestedCount(count)
                .unsafeForEachResultSize(unsafeSize)
                .safeCollectResultSize(safeSize)
                .dataRaceObserved(unsafeSize != count)
                .build();

        return StreamDemoResponse.<PitfallResult>builder()
                .operation("Parallel stream pitfall: shared mutable state")
                .description("Calling list.add(...) from inside a PARALLEL forEach on a plain, non-thread-safe ArrayList is a data race — multiple threads mutate the SAME list concurrently. 'unsafeForEachResultSize' can come back LESS than 'requestedCount' (lost updates) — run this endpoint a few times and watch it vary. 'safeCollectResultSize' uses collect(Collectors.toList()) instead, which merges partial per-thread results safely and ALWAYS equals 'requestedCount'.")
                .codeSnippet("UNSAFE: IntStream.range(0,n).parallel().forEach(list::add)   SAFE: IntStream.range(0,n).parallel().boxed().collect(Collectors.toList())")
                .result(result)
                .build();
    }
}