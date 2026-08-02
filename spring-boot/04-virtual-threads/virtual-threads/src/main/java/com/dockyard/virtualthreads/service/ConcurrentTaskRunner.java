package com.dockyard.virtualthreads.service;

import com.dockyard.virtualthreads.domain.ExecutorKind;
import com.dockyard.virtualthreads.dto.RunSummary;
import com.dockyard.virtualthreads.dto.TaskResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * ConcurrentTaskRunner — fires {@code taskCount} simulated blocking calls at
 * ONE given executor, all at once, and measures how long the WHOLE batch
 * takes to finish. This is the measuring instrument the demo endpoints use to
 * make the virtual-vs-platform difference visible as a single number
 * ({@link RunSummary#totalWallClockMs()}) instead of something you have to
 * take on faith.
 *
 * The submit-all-then-wait-all shape here is intentionally the simplest
 * possible fan-out/fan-in: submit every task immediately (this is what
 * exposes a small platform pool's queueing behavior — 200 tasks land on a
 * 20-thread pool all at once, not trickled in), then block waiting for every
 * {@link Future} to complete.
 */
@Service
@RequiredArgsConstructor
public class ConcurrentTaskRunner {

    private final DownstreamSimulator downstreamSimulator;

    public RunSummary run(ExecutorKind kind, ExecutorService executor, int taskCount, long delayMs) {
        long start = System.nanoTime();

        List<Future<TaskResult>> futures = new ArrayList<>(taskCount);
        for (int i = 0; i < taskCount; i++) {
            int taskId = i;
            futures.add(executor.submit(() -> downstreamSimulator.call(taskId, delayMs)));
        }

        List<TaskResult> results = futures.stream()
                .map(this::awaitResult)
                .collect(Collectors.toList());

        long totalMs = (System.nanoTime() - start) / 1_000_000;

        long distinctThreads = results.stream()
                .map(TaskResult::threadName)
                .distinct()
                .count();

        return RunSummary.builder()
                .executor(kind)
                .taskCount(taskCount)
                .delayMsPerTask(delayMs)
                .totalWallClockMs(totalMs)
                .distinctThreadsUsed((int) distinctThreads)
                // Cap the echoed sample so a request for 10,000 tasks doesn't
                // return a 10,000-element JSON array — just enough to show
                // the thread-naming pattern (virtual-thread-N vs platform-pool-N).
                .sampleResults(results.stream().limit(10).toList())
                .build();
    }

    private TaskResult awaitResult(Future<TaskResult> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while awaiting task result", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Simulated task failed", e.getCause());
        }
    }
}