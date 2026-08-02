package com.dockyard.virtualthreads.dto;

import com.dockyard.virtualthreads.domain.ExecutorKind;
import lombok.Builder;

import java.util.List;

/**
 * RunSummary — the result of running N simulated blocking tasks through ONE
 * executor. The headline number is {@link #totalWallClockMs}: how long it
 * took for ALL tasks to finish, wall-clock. Compare that across two
 * {@code RunSummary}s (one per executor) and the whole story is right there.
 */
@Builder
public record RunSummary(
        ExecutorKind executor,
        int taskCount,
        long delayMsPerTask,
        long totalWallClockMs,
        int distinctThreadsUsed,
        List<TaskResult> sampleResults
) {
}