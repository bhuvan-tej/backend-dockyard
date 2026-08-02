package com.dockyard.virtualthreads.dto;

import lombok.Builder;

/**
 * TaskResult — what one simulated unit of work reports about itself: which
 * thread ran it, whether that thread was virtual, and how long the
 * (simulated) blocking I/O took.
 */
@Builder
public record TaskResult(
        int taskId,
        String threadName,
        boolean virtualThread,
        long delayMs
) {
}