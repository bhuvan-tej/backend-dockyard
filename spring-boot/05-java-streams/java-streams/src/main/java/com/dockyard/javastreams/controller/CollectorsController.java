package com.dockyard.javastreams.controller;

import com.dockyard.javastreams.domain.Employee;
import com.dockyard.javastreams.dto.StreamDemoResponse;
import com.dockyard.javastreams.service.CollectorsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Year;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * CollectorsController — the {@code java.util.stream.Collectors} tour (see
 * {@link CollectorsService} for the detailed "why" behind each one).
 */
@RestController
@RequestMapping("/streams/collectors")
@RequiredArgsConstructor
public class CollectorsController {

    private final CollectorsService service;

    @GetMapping("/tolist")
    public StreamDemoResponse<List<String>> toList() {
        return StreamDemoResponse.<List<String>>builder()
                .operation("Collectors.toList()")
                .description("The simplest collector: drain the stream into a new List.")
                .codeSnippet("employees.stream().map(Employee::name).collect(Collectors.toList())")
                .result(service.namesToList())
                .build();
    }

    @GetMapping("/toset")
    public StreamDemoResponse<Set<String>> toSet() {
        return StreamDemoResponse.<Set<String>>builder()
                .operation("Collectors.toSet()")
                .description("Like toList, but de-duplicates via equals()/hashCode(). Iteration order is not guaranteed.")
                .codeSnippet("employees.stream().map(Employee::department).collect(Collectors.toSet())")
                .result(service.departmentsToSet())
                .build();
    }

    @GetMapping("/tomap")
    public StreamDemoResponse<Map<String, Double>> toMap() {
        return StreamDemoResponse.<Map<String, Double>>builder()
                .operation("Collectors.toMap(keyFn, valueFn)")
                .description("Builds a Map. If two elements produced the SAME key this throws IllegalStateException — it has no merge strategy. Safe here because employee names are unique.")
                .codeSnippet("employees.stream().collect(Collectors.toMap(Employee::name, Employee::salary))")
                .result(service.nameToSalaryMap())
                .build();
    }

    @GetMapping("/tomap-merge")
    public StreamDemoResponse<Map<String, Employee>> toMapWithMerge() {
        return StreamDemoResponse.<Map<String, Employee>>builder()
                .operation("Collectors.toMap(keyFn, valueFn, mergeFn)")
                .description("The 3-argument form adds a MERGE FUNCTION to resolve key collisions instead of throwing. Department collides across many employees, so this keeps whichever one earns more per department.")
                .codeSnippet("employees.stream().collect(Collectors.toMap(Employee::department, e -> e, (a,b) -> a.salary() > b.salary() ? a : b))")
                .result(service.topEarnerPerDepartment())
                .build();
    }

    @GetMapping("/joining")
    public StreamDemoResponse<String> joining() {
        return StreamDemoResponse.<String>builder()
                .operation("Collectors.joining(delimiter, prefix, suffix)")
                .description("Concatenates a Stream<String> into one String, with an optional delimiter/prefix/suffix — no manual StringBuilder loop needed.")
                .codeSnippet("employees.stream().map(Employee::name).collect(Collectors.joining(\", \", \"[\", \"]\"))")
                .result(service.namesJoined())
                .build();
    }

    @GetMapping("/groupingby")
    public StreamDemoResponse<Map<String, List<Employee>>> groupingBy() {
        return StreamDemoResponse.<Map<String, List<Employee>>>builder()
                .operation("Collectors.groupingBy(classifier)")
                .description("Buckets every element by a key function into a Map<Key, List<Element>> — the direct Stream equivalent of SQL's GROUP BY.")
                .codeSnippet("employees.stream().collect(Collectors.groupingBy(Employee::department))")
                .result(service.employeesByDepartment())
                .build();
    }

    @GetMapping("/groupingby-counting")
    public StreamDemoResponse<Map<String, Long>> groupingByCounting() {
        return StreamDemoResponse.<Map<String, Long>>builder()
                .operation("Collectors.groupingBy(classifier, counting())")
                .description("Pairs grouping with a DOWNSTREAM collector applied to each group, instead of always getting a raw List back — here, just the size of each group.")
                .codeSnippet("employees.stream().collect(Collectors.groupingBy(Employee::department, Collectors.counting()))")
                .result(service.countByDepartment())
                .build();
    }

    @GetMapping("/groupingby-mapping")
    public StreamDemoResponse<Map<String, List<String>>> groupingByMapping() {
        return StreamDemoResponse.<Map<String, List<String>>>builder()
                .operation("Collectors.groupingBy(classifier, mapping(fn, toList()))")
                .description("Extracts just ONE field per group instead of the whole element — mapping() adapts a downstream collector to work on a transformed value.")
                .codeSnippet("employees.stream().collect(Collectors.groupingBy(Employee::department, Collectors.mapping(Employee::name, Collectors.toList())))")
                .result(service.namesByDepartment())
                .build();
    }

    @GetMapping("/groupingby-nested")
    public StreamDemoResponse<Map<String, Map<String, List<Employee>>>> groupingByNested() {
        return StreamDemoResponse.<Map<String, Map<String, List<Employee>>>>builder()
                .operation("Nested Collectors.groupingBy(classifier, groupingBy(classifier2))")
                .description("Group by department, then WITHIN each department group again by city. Collectors compose — the downstream of one groupingBy can simply be another groupingBy.")
                .codeSnippet("employees.stream().collect(Collectors.groupingBy(Employee::department, Collectors.groupingBy(Employee::city)))")
                .result(service.departmentThenCity())
                .build();
    }

    @GetMapping("/partitioningby")
    public StreamDemoResponse<Map<Boolean, List<Employee>>> partitioningBy(
            @RequestParam(defaultValue = "5") int yearsThreshold) {
        int currentYear = Year.now(Clock.systemUTC()).getValue();
        return StreamDemoResponse.<Map<Boolean, List<Employee>>>builder()
                .operation("Collectors.partitioningBy(predicate)")
                .description("A SPECIAL CASE of groupingBy where the classifier is a Predicate — the result ALWAYS has exactly two keys, true and false, even if one side is empty. Prefer this over groupingBy whenever the split is genuinely binary.")
                .codeSnippet("employees.stream().collect(Collectors.partitioningBy(e -> (" + currentYear + " - e.joiningYear()) >= " + yearsThreshold + "))")
                .result(service.partitionBySeniority(yearsThreshold, currentYear))
                .build();
    }

    @GetMapping("/summarizing")
    public StreamDemoResponse<DoubleSummaryStatistics> summarizing() {
        return StreamDemoResponse.<DoubleSummaryStatistics>builder()
                .operation("Collectors.summarizingDouble(fn)")
                .description("One pass over the stream, five numbers back at once: count, sum, min, max, average.")
                .codeSnippet("employees.stream().collect(Collectors.summarizingDouble(Employee::salary))")
                .result(service.salaryStatistics())
                .build();
    }

    @GetMapping("/averaging")
    public StreamDemoResponse<Double> averaging() {
        return StreamDemoResponse.<Double>builder()
                .operation("Collectors.averagingInt(fn)")
                .description("A narrower, single-purpose collector when you only need one number instead of the full summary statistics.")
                .codeSnippet("employees.stream().collect(Collectors.averagingInt(Employee::age))")
                .result(service.averageAge())
                .build();
    }

    @GetMapping("/reducing")
    public StreamDemoResponse<Double> reducing() {
        return StreamDemoResponse.<Double>builder()
                .operation("Collectors.reducing(identity, mapper, op)")
                .description("The collector-shaped equivalent of Stream.reduce — rarely needed directly (groupingBy + summingDouble reads better for most real cases) but worth recognizing: several other collectors are built from this internally.")
                .codeSnippet("employees.stream().collect(Collectors.reducing(0.0, Employee::salary, Double::sum))")
                .result(service.totalSalaryViaReducingCollector())
                .build();
    }

    @GetMapping("/summing")
    public StreamDemoResponse<Double> summing() {
        return StreamDemoResponse.<Double>builder()
                .operation("Collectors.summingDouble(fn)")
                .description("Narrower than summarizingDouble: returns just the sum directly, instead of a DoubleSummaryStatistics you'd then pull one field back out of.")
                .codeSnippet("employees.stream().collect(Collectors.summingDouble(Employee::salary))")
                .result(service.totalSalary())
                .build();
    }

    @GetMapping("/tocollection")
    public StreamDemoResponse<LinkedList<String>> toCollection() {
        return StreamDemoResponse.<LinkedList<String>>builder()
                .operation("Collectors.toCollection(supplier)")
                .description("Like toList/toSet, but lets you choose the EXACT concrete collection type to collect into — here a LinkedList, useful whenever the caller specifically needs that type's behaviour.")
                .codeSnippet("employees.stream().map(Employee::name).collect(Collectors.toCollection(LinkedList::new))")
                .result(service.namesToLinkedList())
                .build();
    }

    @GetMapping("/maxby")
    public StreamDemoResponse<Map<String, Optional<Employee>>> maxBy() {
        return StreamDemoResponse.<Map<String, Optional<Employee>>>builder()
                .operation("Collectors.groupingBy(classifier, maxBy(comparator))")
                .description("Collectors.maxBy/minBy are the collector-shaped equivalents of Stream.max/min, most useful as a groupingBy DOWNSTREAM where a plain Stream.max isn't directly reachable. Still Optional-wrapped — an empty group has nothing to report.")
                .codeSnippet("employees.stream().collect(groupingBy(Employee::department, maxBy(comparingDouble(Employee::salary))))")
                .result(service.topEarnerPerDepartmentViaMaxBy())
                .build();
    }
}