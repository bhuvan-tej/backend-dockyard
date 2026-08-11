package com.dockyard.javastreams.service;

import lombok.Builder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ParallelStreamService — {@code stream()} vs {@code parallelStream()}
 * (equivalently, {@code stream().parallel()}).
 *
 * IMPORTANT CONTRAST with the {@code 11-virtual-threads} app: virtual
 * threads help I/O-BOUND work (many blocked, waiting threads). Parallel
 * streams help CPU-BOUND work — a computation with no blocking at all, split
 * across the CPU cores you actually have, using the JVM's common
 * {@link java.util.concurrent.ForkJoinPool}. Neither one is a general
 * "make everything faster" switch; each solves the OPPOSITE kind of
 * bottleneck. That's why this demo uses a CPU-bound sum-of-squares instead of
 * a {@code Thread.sleep}-based simulation.
 */
@Service
public class ParallelStreamService {

    @Builder
    public record TimingResult(long resultValue, long elapsedMillis) {
    }

    /** Deliberately CPU-heavy per element: a busy-loop, no I/O, no sleeping. */
    private long expensiveSquare(int n) {
        long acc = 0;
        for (int i = 0; i < 2000; i++) { // artificial extra work per element
            acc += (long) n * n;
        }
        return acc / 2000;
    }

    public TimingResult sumOfSquaresSequential(int upperBoundExclusive) {
        long start = System.nanoTime();
        long total = IntStream.range(0, upperBoundExclusive)
                .mapToLong(this::expensiveSquare)
                .sum();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        return TimingResult.builder().resultValue(total).elapsedMillis(elapsedMs).build();
    }

    public TimingResult sumOfSquaresParallel(int upperBoundExclusive) {
        long start = System.nanoTime();
        long total = IntStream.range(0, upperBoundExclusive)
                .parallel()
                .mapToLong(this::expensiveSquare)
                .sum();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        return TimingResult.builder().resultValue(total).elapsedMillis(elapsedMs).build();
    }

    /**
     * THE PITFALL: mutating a plain, non-thread-safe {@code ArrayList} from
     * inside a PARALLEL {@code forEach} is a data race. Multiple threads call
     * {@code list.add(...)} concurrently on the SAME list; {@code ArrayList}
     * makes no promise that's safe, and it usually isn't — this typically
     * returns a list with FEWER than {@code count} elements (lost updates)
     * or throws, depending on timing. Run it a few times and the size can
     * even change between runs, which is exactly the "correct on my machine,
     * broken in production" trap parallel streams can spring on unfamiliar code.
     */
    public int unsafeParallelForEachSize(int count) {
        List<Integer> unsafeShared = new ArrayList<>();
        IntStream.range(0, count)
                .parallel()
                .forEach(unsafeShared::add); // DO NOT DO THIS — kept here only to demonstrate the failure
        return unsafeShared.size();
    }

    /**
     * THE FIX: let {@code collect(Collectors.toList())} do the aggregation.
     * Collectors are written to merge partial per-thread results safely, so
     * this always returns EXACTLY {@code count} elements, regardless of how
     * many threads split the work.
     */
    public int safeParallelCollectSize(int count) {
        List<Integer> safeResult = IntStream.range(0, count)
                .parallel()
                .boxed()
                .collect(Collectors.toList());
        return safeResult.size();
    }
}