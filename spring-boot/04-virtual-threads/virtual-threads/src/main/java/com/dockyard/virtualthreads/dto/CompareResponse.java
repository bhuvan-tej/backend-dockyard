package com.dockyard.virtualthreads.dto;

import lombok.Builder;

/**
 * CompareResponse — runs the identical workload through BOTH executors and
 * reports the speedup. With {@code tasks > platform pool size} and a
 * meaningful {@code delayMs}, {@link #speedupFactor} should land well above
 * 1.0 — the platform pool serializes the excess tasks in batches, the
 * virtual-thread executor does not.
 */
@Builder
public record CompareResponse(
        int taskCount,
        long delayMsPerTask,
        RunSummary virtualRun,
        RunSummary platformRun,
        double speedupFactor
) {
    public static CompareResponse of(int taskCount, long delayMs, RunSummary virtualRun, RunSummary platformRun) {
        double speedup = platformRun.totalWallClockMs() / (double) Math.max(1, virtualRun.totalWallClockMs());
        return CompareResponse.builder()
                .taskCount(taskCount)
                .delayMsPerTask(delayMs)
                .virtualRun(virtualRun)
                .platformRun(platformRun)
                .speedupFactor(Math.round(speedup * 100) / 100.0)
                .build();
    }
}