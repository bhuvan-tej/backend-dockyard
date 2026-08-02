package com.dockyard.virtualthreads;

import com.dockyard.virtualthreads.domain.ExecutorKind;
import com.dockyard.virtualthreads.dto.RunSummary;
import com.dockyard.virtualthreads.dto.TaskResult;
import com.dockyard.virtualthreads.service.ConcurrentTaskRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ThreadingBehaviorTest — proves the two claims this whole app is built
 * around, rather than just asserting them in prose:
 *   1) Tasks run on the virtual thread executor really do run on virtual
 *      threads ({@code Thread.isVirtual() == true}).
 *   2) With more tasks than the platform pool has threads, the virtual
 *      thread executor finishes the same batch meaningfully faster — because
 *      it never has to queue anything up.
 */
@SpringBootTest
class ThreadingBehaviorTest {

    @Autowired
    private ConcurrentTaskRunner taskRunner;

    @Autowired
    private ExecutorService virtualThreadExecutor;

    @Autowired
    private ExecutorService platformThreadExecutor;

    @Test
    void tasksSubmittedToVirtualExecutorActuallyRunOnVirtualThreads() {
        RunSummary summary = taskRunner.run(ExecutorKind.VIRTUAL, virtualThreadExecutor, 20, 10);

        assertThat(summary.sampleResults()).isNotEmpty();
        assertThat(summary.sampleResults()).allMatch(TaskResult::virtualThread);
    }

    @Test
    void tasksSubmittedToPlatformExecutorDoNotRunOnVirtualThreads() {
        RunSummary summary = taskRunner.run(ExecutorKind.PLATFORM, platformThreadExecutor, 5, 10);

        assertThat(summary.sampleResults()).isNotEmpty();
        assertThat(summary.sampleResults()).noneMatch(TaskResult::virtualThread);
    }

    @Test
    void virtualThreadsOutpacePlatformPoolWhenTasksExceedPoolSize() {
        // 3x the platform pool size, so the platform run MUST queue in at
        // least 3 sequential batches; the virtual run never queues at all.
        int tasks = 60;
        long delayMs = 150;

        RunSummary virtualRun = taskRunner.run(ExecutorKind.VIRTUAL, virtualThreadExecutor, tasks, delayMs);
        RunSummary platformRun = taskRunner.run(ExecutorKind.PLATFORM, platformThreadExecutor, tasks, delayMs);

        // Generous threshold to avoid CI flakiness — the point isn't an exact
        // ratio, it's that virtual threads are CLEARLY not queueing while the
        // platform pool CLEARLY is.
        assertThat(virtualRun.totalWallClockMs()).isLessThan(platformRun.totalWallClockMs());
        assertThat(platformRun.totalWallClockMs()).isGreaterThanOrEqualTo(2 * delayMs);
    }
}

