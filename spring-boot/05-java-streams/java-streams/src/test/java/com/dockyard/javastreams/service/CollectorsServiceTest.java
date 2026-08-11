package com.dockyard.javastreams.service;

import com.dockyard.javastreams.domain.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CollectorsServiceTest {

    @Autowired
    private CollectorsService service;

    @Test
    void toListReturnsAllTwentyNames() {
        assertThat(service.namesToList()).hasSize(20);
    }

    @Test
    void toSetHasFiveUniqueDepartments() {
        assertThat(service.departmentsToSet()).hasSize(5);
    }

    @Test
    void toMapKeysByUniqueName() {
        Map<String, Double> map = service.nameToSalaryMap();
        assertThat(map).hasSize(20);
        assertThat(map.get("Alice Chen")).isEqualTo(95000.0);
    }

    @Test
    void toMapWithMergeKeepsHigherEarnerPerDepartment() {
        Map<String, Employee> topPerDept = service.topEarnerPerDepartment();
        assertThat(topPerDept).hasSize(5);
        // Engineering's highest earner is David O'Neil at 142000.
        assertThat(topPerDept.get("Engineering").name()).isEqualTo("David O'Neil");
    }

    @Test
    void joiningWrapsWithBracketsAndCommas() {
        String joined = service.namesJoined();
        assertThat(joined).startsWith("[").endsWith("]").contains(", ");
    }

    @Test
    void groupingByProducesFiveDepartmentBuckets() {
        assertThat(service.employeesByDepartment()).hasSize(5);
    }

    @Test
    void groupingByCountingSumsToTwenty() {
        long total = service.countByDepartment().values().stream().mapToLong(Long::longValue).sum();
        assertThat(total).isEqualTo(20);
    }

    @Test
    void groupingByMappingExtractsOnlyNames() {
        Map<String, java.util.List<String>> namesByDept = service.namesByDepartment();
        assertThat(namesByDept.get("HR")).containsExactlyInAnyOrder("Harish Rao", "Isha Kapoor", "Rohit Bansal");
    }

    @Test
    void nestedGroupingByHasDepartmentThenCityLevels() {
        var nested = service.departmentThenCity();
        assertThat(nested).containsKey("Engineering");
        assertThat(nested.get("Engineering")).containsKey("Bengaluru");
    }

    @Test
    void partitioningByAlwaysHasExactlyTwoKeys() {
        Map<Boolean, java.util.List<Employee>> partitioned = service.partitionBySeniority(100, 2026);
        assertThat(partitioned).containsKeys(true, false);
    }

    @Test
    void summarizingStatisticsCoverAllEmployees() {
        assertThat(service.salaryStatistics().getCount()).isEqualTo(20);
    }

    @Test
    void averagingAndReducingProduceSaneNumbers() {
        assertThat(service.averageAge()).isGreaterThan(0);
        assertThat(service.totalSalaryViaReducingCollector()).isGreaterThan(0);
    }

    @Test
    void summingDoubleMatchesReducingCollectorTotal() {
        assertThat(service.totalSalary()).isEqualTo(service.totalSalaryViaReducingCollector());
    }

    @Test
    void toCollectionReturnsALinkedListOfAllTwentyNames() {
        assertThat(service.namesToLinkedList()).hasSize(20).isInstanceOf(java.util.LinkedList.class);
    }

    @Test
    void maxByProducesOnePresentOptionalPerDepartment() {
        Map<String, java.util.Optional<Employee>> topPerDept = service.topEarnerPerDepartmentViaMaxBy();
        assertThat(topPerDept).hasSize(5);
        assertThat(topPerDept.get("Engineering")).isPresent();
        assertThat(topPerDept.get("Engineering").get().name()).isEqualTo("David O'Neil");
    }
}

