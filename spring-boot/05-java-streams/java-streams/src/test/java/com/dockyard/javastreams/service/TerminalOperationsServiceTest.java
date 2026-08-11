package com.dockyard.javastreams.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TerminalOperationsServiceTest {

    @Autowired
    private TerminalOperationsService service;

    @Test
    void forEachCollectsOnlyEngineeringNames() {
        assertThat(service.forEachCollectNames()).isNotEmpty();
    }

    @Test
    void countMatchesFilterSize() {
        assertThat(service.countInDepartment("Finance")).isEqualTo(4);
    }

    @Test
    void maxAndMinAreOppositeEnds() {
        double highest = service.highestPaid().orElseThrow().salary();
        double lowest = service.lowestPaid().orElseThrow().salary();
        assertThat(highest).isGreaterThan(lowest);
    }

    @Test
    void anyMatchIsTrueForALowThreshold() {
        assertThat(service.anyoneEarnsOver(1.0)).isTrue();
    }

    @Test
    void anyMatchIsFalseForAnImpossibleThreshold() {
        assertThat(service.anyoneEarnsOver(10_000_000)).isFalse();
    }

    @Test
    void allMatchIsTrueWhenEveryoneQualifies() {
        assertThat(service.everyoneIsAdult(18)).isTrue();
    }

    @Test
    void noneMatchIsTrueWhenFloorIsBelowEveryone() {
        assertThat(service.noOneUnderpaid(1.0)).isTrue();
    }

    @Test
    void findFirstReturnsAMatchingEmployee() {
        assertThat(service.firstInDepartment("Sales")).isPresent();
        assertThat(service.firstInDepartment("Sales").orElseThrow().department()).isEqualTo("Sales");
    }

    @Test
    void findAnyOnUnknownCityIsEmpty() {
        assertThat(service.anyInCity("Nowhere")).isEmpty();
    }

    @Test
    void reduceNoIdentityMatchesMax() {
        double viaReduce = service.reduceMaxSalaryNoIdentity().orElseThrow();
        double viaMax = service.highestPaid().orElseThrow().salary();
        assertThat(viaReduce).isEqualTo(viaMax);
    }

    @Test
    void reduceWithIdentitySumsAllSalaries() {
        assertThat(service.reduceTotalSalary()).isGreaterThan(0);
    }

    @Test
    void reduceWithCombinerCountsCorrectly() {
        int viaReduce = service.reduceCountAboveSalary(100_000);
        assertThat(viaReduce).isGreaterThan(0);
    }

    @Test
    void forEachSideEffectCountsAllTwenty() {
        assertThat(service.forEachSideEffectCount()).isEqualTo(20);
    }
}


