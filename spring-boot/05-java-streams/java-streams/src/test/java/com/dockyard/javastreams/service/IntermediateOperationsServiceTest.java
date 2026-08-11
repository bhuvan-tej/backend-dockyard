package com.dockyard.javastreams.service;

import com.dockyard.javastreams.domain.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IntermediateOperationsServiceTest {

    @Autowired
    private IntermediateOperationsService service;

    @Test
    void filterByDepartmentReturnsOnlyThatDepartment() {
        List<Employee> engineers = service.filterByDepartment("Engineering");
        assertThat(engineers).isNotEmpty();
        assertThat(engineers).allMatch(e -> e.department().equals("Engineering"));
    }

    @Test
    void mapToNamesReturnsAllTwentyNames() {
        assertThat(service.mapToNames()).hasSize(20);
    }

    @Test
    void flatMapProducesDistinctSortedTags() {
        List<String> tags = service.flatMapSkillTags();
        assertThat(tags).isSorted();
        assertThat(tags).doesNotHaveDuplicates();
        assertThat(tags).contains("Engineering", "Bengaluru");
    }

    @Test
    void distinctDepartmentsHasFiveUniqueValues() {
        assertThat(service.distinctDepartments()).containsExactlyInAnyOrder(
                "Engineering", "Sales", "HR", "Marketing", "Finance");
    }

    @Test
    void sortedNamesAreAlphabetical() {
        assertThat(service.sortedNames()).isSorted();
    }

    @Test
    void sortedBySalaryDescStartsWithHighestEarner() {
        List<Employee> sorted = service.sortedBySalaryDesc();
        assertThat(sorted.get(0).salary()).isGreaterThanOrEqualTo(sorted.get(sorted.size() - 1).salary());
    }

    @Test
    void peekTraceRecordsFilterThenMapPerElement() {
        List<String> trace = new ArrayList<>();
        service.peekTrace(trace);
        assertThat(trace).isNotEmpty();
        // First two trace entries for the FIRST matching element should be "passed filter" then "after map" —
        // proving peek fires element-by-element through the WHOLE pipeline, not phase-by-phase.
        assertThat(trace.get(0)).startsWith("passed filter:");
        assertThat(trace.get(1)).startsWith("after map:");
    }

    @Test
    void limitReturnsExactlyRequestedCount() {
        assertThat(service.topEarners(3)).hasSize(3);
    }

    @Test
    void skipDropsTheFirstNElements() {
        List<Employee> all = service.allButTopEarners(0);
        List<Employee> skipped = service.allButTopEarners(3);
        assertThat(skipped).hasSize(all.size() - 3);
    }
}

