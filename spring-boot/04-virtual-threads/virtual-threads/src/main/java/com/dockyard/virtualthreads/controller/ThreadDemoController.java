package com.dockyard.virtualthreads.controller;

import com.dockyard.virtualthreads.domain.ExecutorKind;
import com.dockyard.virtualthreads.dto.CompareResponse;
import com.dockyard.virtualthreads.dto.RunSummary;
import com.dockyard.virtualthreads.dto.ThreadInfo;
import com.dockyard.virtualthreads.service.ConcurrentTaskRunner;
import com.dockyard.virtualthreads.service.DownstreamSimulator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;

/**
 * ThreadDemoController — every endpoint that makes virtual vs platform
 * threads visible and measurable.
 *
 * TRY THIS FIRST (the single clearest demo):
 *   GET /demo/compare?tasks=100&delayMs=200
 * With a 20-thread platform pool ({@code ThreadingConfig.PLATFORM_POOL_SIZE}),
 * 100 tasks that each take 200ms should take the platform pool roughly
 * (100/20) * 200ms ≈ 1000ms, while the virtual-thread executor finishes in
 * roughly 200ms — because it never has to queue anything.
 */
@RestController
@RequestMapping("/demo")
@RequiredArgsConstructor
@Validated
public class ThreadDemoController {

    private final ConcurrentTaskRunner taskRunner;
    private final DownstreamSimulator downstreamSimulator;
    private final ExecutorService virtualThreadExecutor;
    private final ExecutorService platformThreadExecutor;

    /**
     * Runs {@code tasks} simulated blocking calls through ONE named executor
     * and reports how long the whole batch took.
     */
    @GetMapping("/run")
    public RunSummary run(
            @RequestParam(defaultValue = "VIRTUAL") ExecutorKind executor,
            @RequestParam(defaultValue = "100") @Min(1) @Max(10_000) int tasks,
            @RequestParam(defaultValue = "200") @Min(0) @Max(10_000) long delayMs) {

        ExecutorService target = (executor == ExecutorKind.VIRTUAL)
                ? virtualThreadExecutor
                : platformThreadExecutor;

        return taskRunner.run(executor, target, tasks, delayMs);
    }

    /**
     * Runs the IDENTICAL workload through both executors, back to back, and
     * reports the speedup — the one endpoint that tells the whole story.
     */
    @GetMapping("/compare")
    public CompareResponse compare(
            @RequestParam(defaultValue = "100") @Min(1) @Max(10_000) int tasks,
            @RequestParam(defaultValue = "200") @Min(0) @Max(10_000) long delayMs) {

        RunSummary virtualRun = taskRunner.run(ExecutorKind.VIRTUAL, virtualThreadExecutor, tasks, delayMs);
        RunSummary platformRun = taskRunner.run(ExecutorKind.PLATFORM, platformThreadExecutor, tasks, delayMs);

        return CompareResponse.of(tasks, delayMs, virtualRun, platformRun);
    }

    /**
     * Reports which thread is handling THIS HTTP request. With
     * {@code spring.threads.virtual.enabled: true}, this will always be a
     * virtual thread — call it many times concurrently (e.g. `ab -c 200`) and
     * every single request still gets served, with no pool-exhaustion queueing,
     * because Tomcat itself is now handing requests to virtual threads.
     */
    @GetMapping("/request-thread")
    public ThreadInfo requestThread(
            @RequestParam(defaultValue = "0") @Min(0) @Max(10_000) long delayMs) {

        Thread current = Thread.currentThread();
        if (delayMs > 0) {
            // Reuse the same simulator so this endpoint feels exactly like the
            // batch ones — just observed from the request-handling thread itself.
            downstreamSimulator.call(0, delayMs);
        }
        return ThreadInfo.builder()
                .threadName(current.toString())
                .virtualThread(current.isVirtual())
                .delayMs(delayMs)
                .build();
    }
}