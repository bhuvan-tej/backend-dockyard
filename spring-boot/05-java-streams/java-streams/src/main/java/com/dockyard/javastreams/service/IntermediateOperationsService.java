package com.dockyard.javastreams.service;

import com.dockyard.javastreams.dataset.EmployeeDataset;
import com.dockyard.javastreams.domain.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * IntermediateOperationsService — operations that transform a stream into
 * ANOTHER stream, without consuming it. This is the key idea to internalize:
 * NOTHING in this class actually runs anything by itself. Every method here
 * builds up a pipeline description; the pipeline only executes once a
 * TERMINAL operation (here, always {@code toList}) is attached at the end.
 * This is why streams are called LAZY.
 *
 * A second distinction worth knowing: {@code filter}/{@code map}/{@code peek}
 * are STATELESS — each element is processed independently, with no memory of
 * elements seen before. {@code distinct}/{@code sorted} are STATEFUL — they
 * need to see the WHOLE stream (or at least track everything seen so far)
 * before they can produce their first output element.
 */
@Service
@RequiredArgsConstructor
public class IntermediateOperationsService {

    private final EmployeeDataset dataset;

    /** filter — keeps only elements matching a predicate; stateless, can short-circuit downstream. */
    public List<Employee> filterByDepartment(String department) {
        return dataset.all().stream()
                .filter(e -> e.department().equalsIgnoreCase(department))
                .toList();
    }

    /** map — transforms EACH element 1-to-1 into something else; stateless. */
    public List<String> mapToNames() {
        return dataset.all().stream()
                .map(Employee::name)
                .toList();
    }

    /**
     * flatMap — transforms each element into its OWN stream, then flattens
     * all those little streams into one. Needed whenever "map" would produce
     * a stream-of-streams (e.g. one employee -> several skill tags) and you
     * want a single flat sequence instead.
     */
    public List<String> flatMapSkillTags() {
        // Each employee "has" department + city as simple tags, standing in for
        // a real one-to-many relationship (e.g. an employee's list of skills).
        return dataset.all().stream()
                .flatMap(e -> Stream.of(e.department(), e.city()))
                .distinct()
                .sorted()
                .toList();
    }

    /** distinct — removes duplicates using equals()/hashCode(); stateful (must remember what it's seen). */
    public List<String> distinctDepartments() {
        return dataset.all().stream()
                .map(Employee::department)
                .distinct()
                .toList();
    }

    /** sorted — natural ordering (requires Comparable); stateful (must see every element first). */
    public List<String> sortedNames() {
        return dataset.all().stream()
                .map(Employee::name)
                .sorted()
                .toList();
    }

    /**
     * sorted(Comparator) — custom ordering. {@code Comparator.comparing} +
     * {@code thenComparing} (both Java 8) compose a multi-key sort readably,
     * instead of a hand-written compare() method.
     */
    public List<Employee> sortedBySalaryDesc() {
        return dataset.all().stream()
                .sorted(Comparator.comparingDouble(Employee::salary).reversed()
                        .thenComparing(Employee::name))
                .toList();
    }

    /**
     * peek — runs a side-effecting action on each element AS IT PASSES
     * THROUGH, without changing the stream. Intended for debugging a
     * pipeline mid-flight, NOT as a substitute for {@code forEach} or
     * {@code map} — relying on peek for real work is a well-known pitfall
     * (see LEARNING.md).
     */
    public List<String> peekTrace(List<String> trace) {
        return dataset.all().stream()
                .filter(e -> e.salary() > 100_000)
                .peek(e -> trace.add("passed filter: " + e.name()))
                .map(Employee::name)
                .peek(e -> trace.add("after map: " + e))
                .toList();
    }

    /** limit — truncates the stream to at most N elements; short-circuiting. */
    public List<Employee> topEarners(long limit) {
        return dataset.all().stream()
                .sorted(Comparator.comparingDouble(Employee::salary).reversed())
                .limit(limit)
                .toList();
    }

    /** skip — discards the first N elements, keeping the rest; must still traverse the skipped ones. */
    public List<Employee> allButTopEarners(long skip) {
        return dataset.all().stream()
                .sorted(Comparator.comparingDouble(Employee::salary).reversed())
                .skip(skip)
                .toList();
    }
}