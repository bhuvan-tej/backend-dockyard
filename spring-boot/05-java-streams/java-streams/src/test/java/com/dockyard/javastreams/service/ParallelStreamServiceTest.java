package com.dockyard.javastreams.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ParallelStreamServiceTest {

    @Autowired
    private ParallelStreamService service;

    @Test
    void sequentialAndParallelProduceTheSameResult() {
        var sequential = service.sumOfSquaresSequential(2000);
        var parallel = service.sumOfSquaresParallel(2000);
        assertThat(parallel.resultValue()).isEqualTo(sequential.resultValue());
    }

    @Test
    void safeCollectAlwaysReturnsExactCount() {
        // Run several times — the SAFE path must never lose an element, unlike the unsafe one.
        for (int i = 0; i < 5; i++) {
            assertThat(service.safeParallelCollectSize(20_000)).isEqualTo(20_000);
        }
    }
}

