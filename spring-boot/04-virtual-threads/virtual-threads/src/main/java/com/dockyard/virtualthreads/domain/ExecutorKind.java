package com.dockyard.virtualthreads.domain;

/**
 * ExecutorKind — the two executor flavors this app lets you pick between on
 * the demo endpoints, so the same workload can be run through either one and
 * compared honestly (same tasks, same simulated delay — only the executor
 * that runs them changes).
 */
public enum ExecutorKind {
    VIRTUAL,
    PLATFORM
}