package com.dockyard.virtualthreads.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ThreadingConfig — the two executors this whole app exists to compare.
 *
 * <h2>1) The virtual thread executor</h2>
 * {@link Executors#newVirtualThreadPerTaskExecutor()} creates a NEW virtual
 * thread for every single task submitted to it — there is no pool to size,
 * no queue to overflow, because virtual threads are cheap enough (a few
 * hundred bytes each, not the ~1MB stack of a platform thread) that "one per
 * task, however many tasks that is" is a perfectly reasonable strategy.
 *
 * <h2>2) The platform thread executor</h2>
 * A traditional {@link Executors#newFixedThreadPool(int)} backed by real OS
 * threads. Deliberately sized SMALL ({@link #PLATFORM_POOL_SIZE}) so the demo
 * endpoints can show what happens when concurrent I/O-bound work (tasks that
 * mostly just wait) outnumbers the threads available to run it: tasks queue
 * up and total time grows in proportion to (tasks / poolSize), instead of
 * staying roughly flat like the virtual-thread version does.
 *
 * <h2>Why this matters for I/O-bound work specifically</h2>
 * Virtual threads do NOT make your CPU faster — a tight, CPU-bound loop runs
 * at the same speed either way, because it never blocks and so never frees
 * its carrier thread. The win is specifically for I/O-bound work: HTTP calls
 * to other services, JDBC queries, {@code Thread.sleep} — anything where the
 * thread would otherwise sit idle waiting. That's exactly what
 * {@code DownstreamSimulator} simulates with a plain {@code Thread.sleep}.
 */
@Slf4j
@Configuration
public class ThreadingConfig {

    /** Deliberately small — see class-level Javadoc for why. */
    public static final int PLATFORM_POOL_SIZE = 20;

    @Bean
    public ExecutorService virtualThreadExecutor() {
        log.info("Created virtual-thread-per-task executor (unbounded — one virtual thread per task)");
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public ExecutorService platformThreadExecutor() {
        log.info("Created fixed platform thread pool executor (size={})", PLATFORM_POOL_SIZE);
        return Executors.newFixedThreadPool(PLATFORM_POOL_SIZE, runnable -> {
            Thread t = new Thread(runnable);
            t.setName("platform-pool-" + t.threadId());
            return t;
        });
    }
}

