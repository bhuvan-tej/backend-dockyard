package com.dockyard.javastreams.service;

import com.dockyard.javastreams.dataset.EmployeeDataset;
import com.dockyard.javastreams.domain.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * CollectorsService — {@code java.util.stream.Collectors} is arguably the
 * single most-used part of the Stream API in real code, because it's how you
 * turn a stream back into a familiar shape (List, Set, Map, grouped report)
 * once you're done transforming it. Every method here demonstrates ONE
 * collector, building in complexity from "collect into a List" up to
 * multi-level grouped reports.
 */
@Service
@RequiredArgsConstructor
public class CollectorsService {

    private final EmployeeDataset dataset;

    /** toList — the simplest collector: drain the stream into a new List. */
    public List<String> namesToList() {
        return dataset.all().stream()
                .map(Employee::name)
                .toList();
    }

    /** toSet — like toList, but de-duplicates via equals()/hashCode(); order not guaranteed. */
    public Set<String> departmentsToSet() {
        return dataset.all().stream()
                .map(Employee::department)
                .collect(Collectors.toSet());
    }

    /**
     * toMap(keyFn, valueFn) — builds a Map. IMPORTANT: if two elements
     * produce the SAME key, this two-argument form throws
     * {@code IllegalStateException} at runtime — it has no idea which value
     * should win. Names happen to be unique here, so it's safe.
     */
    public Map<String, Double> nameToSalaryMap() {
        return dataset.all().stream()
                .collect(Collectors.toMap(Employee::name, Employee::salary));
    }

    /**
     * toMap(keyFn, valueFn, mergeFn) — the three-argument form adds a MERGE
     * FUNCTION that resolves collisions instead of throwing. Here, department
     * is NOT unique per employee, so colliding on department and keeping
     * whichever employee earns more demonstrates why the merge function
     * exists at all.
     */
    public Map<String, Employee> topEarnerPerDepartment() {
        return dataset.all().stream()
                .collect(Collectors.toMap(
                        Employee::department,
                        employee -> employee,
                        (existing, candidate) -> candidate.salary() > existing.salary() ? candidate : existing));
    }

    /**
     * joining — concatenates a Stream<String> into one String. The
     * three-argument overload adds a delimiter, prefix and suffix — useful
     * for building human-readable lists without a manual StringBuilder loop.
     */
    public String namesJoined() {
        return dataset.all().stream()
                .map(Employee::name)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * groupingBy(classifier) — the single most useful collector for
     * reporting: buckets every element by a KEY FUNCTION into a
     * {@code Map<Key, List<Element>>}. This is the direct Stream equivalent
     * of a SQL {@code GROUP BY}.
     */
    public Map<String, List<Employee>> employeesByDepartment() {
        return dataset.all().stream()
                .collect(Collectors.groupingBy(Employee::department));
    }

    /**
     * groupingBy(classifier, downstream) — pairs the grouping with a SECOND
     * collector applied to each group's elements, instead of always getting
     * a raw List back. Here: how many people per department.
     */
    public Map<String, Long> countByDepartment() {
        return dataset.all().stream()
                .collect(Collectors.groupingBy(Employee::department, Collectors.counting()));
    }

    /** groupingBy + mapping — extracts just ONE field per group instead of the whole Employee. */
    public Map<String, List<String>> namesByDepartment() {
        return dataset.all().stream()
                .collect(Collectors.groupingBy(
                        Employee::department,
                        Collectors.mapping(Employee::name, Collectors.toList())));
    }

    /**
     * Nested groupingBy — group by department, then WITHIN each department
     * group again by city. The downstream of the outer groupingBy is simply
     * ANOTHER groupingBy — collectors compose.
     */
    public Map<String, Map<String, List<Employee>>> departmentThenCity() {
        return dataset.all().stream()
                .collect(Collectors.groupingBy(
                        Employee::department,
                        Collectors.groupingBy(Employee::city)));
    }

    /**
     * partitioningBy — a SPECIAL CASE of groupingBy where the classifier is
     * a {@code Predicate}, so the result ALWAYS has exactly two keys:
     * {@code true} and {@code false} — even if one group is empty. Prefer
     * this over {@code groupingBy} whenever the split is genuinely binary.
     */
    public Map<Boolean, List<Employee>> partitionBySeniority(int yearsThreshold, int currentYear) {
        return dataset.all().stream()
                .collect(Collectors.partitioningBy(e -> (currentYear - e.joiningYear()) >= yearsThreshold));
    }

    /** summarizingDouble — one pass, five numbers: count, sum, min, max, average. */
    public DoubleSummaryStatistics salaryStatistics() {
        return dataset.all().stream()
                .collect(Collectors.summarizingDouble(Employee::salary));
    }

    /** averagingDouble / summingDouble — narrower single-purpose collectors when you only need one number. */
    public double averageAge() {
        return dataset.all().stream()
                .collect(Collectors.averagingInt(Employee::age));
    }

    /**
     * Collectors.reducing — the collector-shaped equivalent of
     * {@code Stream.reduce}. Rarely needed directly (most reductions read
     * better as {@code groupingBy(..., summingDouble(...))} etc.) but useful
     * to recognize: it's what several other collectors are built from
     * internally.
     */
    public double totalSalaryViaReducingCollector() {
        return dataset.all().stream()
                .collect(Collectors.reducing(0.0, Employee::salary, Double::sum));
    }

    /**
     * Collectors.summingDouble — narrower than summarizingDouble: returns
     * just the ONE number (the sum) directly, instead of a
     * DoubleSummaryStatistics you'd then have to pull a field out of.
     */
    public double totalSalary() {
        return dataset.all().stream()
                .collect(Collectors.summingDouble(Employee::salary));
    }

    /**
     * Collectors.toCollection(supplier) — like toList/toSet, but lets you
     * choose the EXACT concrete collection type to collect into (here, a
     * LinkedList instead of whatever toList() happens to use internally) —
     * useful whenever the caller specifically needs that type's behaviour
     * (e.g. a LinkedList's O(1) head/tail removal, or a TreeSet's ordering).
     */
    public LinkedList<String> namesToLinkedList() {
        return dataset.all().stream()
                .map(Employee::name)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Collectors.minBy/maxBy — the collector-shaped equivalent of
     * {@code Stream.min}/{@code Stream.max}, most useful as the DOWNSTREAM
     * of a groupingBy (see {@code CollectorsController#topEarnerPerDepartmentViaMaxBy}),
     * where {@code Stream.max} isn't directly available. Both return an
     * {@code Optional} for the same reason plain {@code max}/{@code min} do:
     * an empty group has nothing to report.
     */
    public Map<String, Optional<Employee>> topEarnerPerDepartmentViaMaxBy() {
        return dataset.all().stream()
                .collect(Collectors.groupingBy(
                        Employee::department,
                        Collectors.maxBy(Comparator.comparingDouble(Employee::salary))));
    }
}