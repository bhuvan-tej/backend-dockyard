package com.dockyard.javastreams.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StreamCreationServiceTest {

    @Autowired
    private StreamCreationService service;

    @Test
    void fromCollectionUppercases() {
        assertThat(service.fromCollection(List.of("a", "b"))).containsExactly("A", "B");
    }

    @Test
    void fromVarargsUppercasesFixedWords() {
        assertThat(service.fromVarargs()).contains("STREAM", "MAP", "FILTER", "REDUCE", "COLLECT");
    }

    @Test
    void fromArraySortsAscending() {
        assertThat(service.fromArray()).containsExactly(1, 2, 3, 5, 8, 9);
    }

    @Test
    void fromIntRangeExcludesUpperBound() {
        assertThat(service.fromIntRange(0, 5)).containsExactly(0, 1, 2, 3, 4);
    }

    @Test
    void fromGenerateProducesExactlyRequestedCount() {
        assertThat(service.fromGenerate(7)).hasSize(7);
    }

    @Test
    void fromIterateDoublesEachStep() {
        assertThat(service.fromIterate(1, 5)).containsExactly(1, 2, 4, 8, 16);
    }

    @Test
    void emptyStreamProducesEmptyList() {
        assertThat(service.empty()).isEmpty();
    }

    @Test
    void concatPreservesSourceOrder() {
        assertThat(service.concatenated()).containsExactly("a", "b", "c", "x", "y", "z");
    }
}

