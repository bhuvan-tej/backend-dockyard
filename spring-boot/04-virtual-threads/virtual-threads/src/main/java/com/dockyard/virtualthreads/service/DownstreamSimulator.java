package com.dockyard.virtualthreads.service;

import com.dockyard.virtualthreads.dto.TaskResult;
import org.springframework.stereotype.Service;

/**
 * DownstreamSimulator — stands in for "a slow, blocking I/O call": a JDBC
 * query, an HTTP call to another microservice, a file read over NFS, etc.
 *
 * It uses a plain {@link Thread#sleep(long)} on purpose — NOT because sleep
 * itself is interesting, but because it's the simplest possible stand-in for
 * ANY blocking call. The important detail for this whole demo: when a
 * VIRTUAL thread calls {@code Thread.sleep}, the JVM unmounts it from its
 * carrier platform thread for the duration of the sleep, freeing that
 * carrier to run other virtual threads. A PLATFORM thread calling the same
 * {@code Thread.sleep} just blocks the OS thread, full stop — that thread is
 * unavailable to do anything else until the sleep ends. Same line of code,
 * completely different cost, depending only on which kind of thread runs it.
 */
@Service
public class DownstreamSimulator {

    /**
     * Simulates one blocking call that takes {@code delayMs} to complete, and
     * reports exactly which thread (and what kind) ended up doing the waiting.
     */
    public TaskResult call(int taskId, long delayMs) {
        Thread current = Thread.currentThread();
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            current.interrupt();
            throw new IllegalStateException("Simulated downstream call interrupted", e);
        }
        return TaskResult.builder()
                .taskId(taskId)
                .threadName(current.toString())
                .virtualThread(current.isVirtual())
                .delayMs(delayMs)
                .build();
    }
}