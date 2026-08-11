package com.dockyard.javastreams.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PrimitiveStreamOperationsServiceTest {

    @Autowired
    private PrimitiveStreamOperationsService service;

    @Test
    void totalAgeYearsSumsAllTwentyAges() {
        assertThat(service.totalAgeYears()).isGreaterThan(0);
    }

    @Test
    void averageAgeIsPresentForNonEmptyDataset() {
        assertThat(service.averageAge()).isPresent();
        assertThat(service.averageAge().getAsDouble()).isGreaterThan(0);
    }

    @Test
    void oldestAgeIsPresent() {
        assertThat(service.oldestAge()).isPresent();
    }

    @Test
    void ageStatisticsCoverAllTwentyEmployees() {
        assertThat(service.ageStatistics().getCount()).isEqualTo(20);
    }

    @Test
    void salaryStatisticsViaPrimitiveStreamCoverAllTwentyEmployees() {
        assertThat(service.salaryStatisticsViaPrimitiveStream().getCount()).isEqualTo(20);
    }

    @Test
    void sortedUniqueAgesAreSortedAndDistinct() {
        List<Integer> ages = service.sortedUniqueAges();
        assertThat(ages).isSorted();
        assertThat(ages).doesNotHaveDuplicates();
    }

    @Test
    void sumOfEvenNumbersUpToTenIsThirty() {
        assertThat(service.sumOfEvenNumbersUpTo(10)).isEqualTo(2 + 4 + 6 + 8 + 10);
    }
}

