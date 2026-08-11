package com.dockyard.javastreams.service;

import com.dockyard.javastreams.domain.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ModernStreamOperationsServiceTest {

    @Autowired
    private ModernStreamOperationsService service;

    @Test
    void toListShorthandReturnsAllTwentyNamesAndIsUnmodifiable() {
        List<String> names = service.toListShorthand();
        assertThat(names).hasSize(20);
        assertThatThrownByAddingTo(names);
    }

    private void assertThatThrownByAddingTo(List<String> names) {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> names.add("nope"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void takeWhileStopsAtFirstNonMatchingElement() {
        List<Employee> before2018 = service.takeWhileJoinedBefore(2018);
        assertThat(before2018).allMatch(e -> e.joiningYear() < 2018);
    }

    @Test
    void dropWhileKeepsEverythingAfterFirstNonMatch() {
        List<Employee> before2018 = service.takeWhileJoinedBefore(2018);
        List<Employee> from2018Onwards = service.dropWhileJoinedBefore(2018);
        assertThat(before2018.size() + from2018Onwards.size()).isEqualTo(20);
    }

    @Test
    void teeingComputesAverageAndCountInOnePass() {
        ModernStreamOperationsService.SalarySummary summary = service.teeingAverageAndCount();
        assertThat(summary.count()).isEqualTo(20);
        assertThat(summary.averageSalary()).isGreaterThan(0);
    }

    @Test
    void ofNullableLookupSkipsMissingIds() {
        List<Employee> found = service.ofNullableLookup(List.of(1L, 999L, 2L));
        assertThat(found).hasSize(2);
    }

    @Test
    void optionalStreamLookupSkipsMissingIds() {
        List<Employee> found = service.optionalStreamLookup(List.of(1L, 999L, 2L));
        assertThat(found).hasSize(2);
    }

    @Test
    void mapMultiProducesDistinctSortedTags() {
        List<String> tags = service.mapMultiTags();
        assertThat(tags).isSorted();
        assertThat(tags).doesNotHaveDuplicates();
        assertThat(tags).contains("Engineering", "Bengaluru");
    }

    @Test
    void filteringWithinGroupsKeepsEveryDepartmentKey() {
        Map<String, List<Employee>> grouped = service.filteringWithinGroups(1_000_000);
        assertThat(grouped).containsKeys("Engineering", "Sales", "HR", "Marketing", "Finance");
        assertThat(grouped.values()).allMatch(List::isEmpty);
    }

    @Test
    void iterateWithPredicateStopsAtBound() {
        List<Integer> result = service.iterateWithPredicate(1, 100);
        assertThat(result).containsExactly(1, 2, 4, 8, 16, 32, 64);
    }
}

