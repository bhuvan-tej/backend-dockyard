package com.dockyard.javastreams.controller;

import com.dockyard.javastreams.domain.Employee;
import com.dockyard.javastreams.dto.StreamDemoResponse;
import com.dockyard.javastreams.service.ModernStreamOperationsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * ModernStreamOperationsController — the Stream/Collectors additions from
 * AFTER Java 8 (Java 9 through Java 16). Deliberately a SEPARATE controller
 * (and its own service, {@link ModernStreamOperationsService}) from every
 * other controller in this app, which is strictly Java-8-vocabulary-only —
 * see LEARNING.md section 9 for why that restriction exists, and why these
 * newer additions are still worth knowing about.
 */
@RestController
@RequestMapping("/streams/modern")
@RequiredArgsConstructor
@Validated
public class ModernStreamOperationsController {

    private final ModernStreamOperationsService service;

    @GetMapping("/tolist")
    public StreamDemoResponse<List<String>> toList() {
        return StreamDemoResponse.<List<String>>builder()
                .operation("Stream.toList() — Java 16")
                .description("Shorthand for collect(Collectors.toUnmodifiableList()). Returns an UNMODIFIABLE list — add()/remove() on the result throws UnsupportedOperationException, unlike the mutable ArrayList that Collectors.toList() hands back.")
                .codeSnippet("employees.stream().map(Employee::name).toList()")
                .result(service.toListShorthand())
                .build();
    }

    @GetMapping("/takewhile")
    public StreamDemoResponse<List<Employee>> takeWhile(@RequestParam(defaultValue = "2018") int year) {
        return StreamDemoResponse.<List<Employee>>builder()
                .operation("Stream.takeWhile — Java 9")
                .description("Takes elements from the front of an ORDERED stream while the predicate holds, then STOPS at the first failure — even if a later element would have matched. Contrast with filter, which judges every element independently regardless of position.")
                .codeSnippet("employees.stream().sorted(comparingInt(Employee::joiningYear)).takeWhile(e -> e.joiningYear() < " + year + ")")
                .result(service.takeWhileJoinedBefore(year))
                .build();
    }

    @GetMapping("/dropwhile")
    public StreamDemoResponse<List<Employee>> dropWhile(@RequestParam(defaultValue = "2018") int year) {
        return StreamDemoResponse.<List<Employee>>builder()
                .operation("Stream.dropWhile — Java 9")
                .description("The mirror image of takeWhile: discards elements from the front while the predicate holds, then keeps everything after that point regardless of whether it still matches.")
                .codeSnippet("employees.stream().sorted(comparingInt(Employee::joiningYear)).dropWhile(e -> e.joiningYear() < " + year + ")")
                .result(service.dropWhileJoinedBefore(year))
                .build();
    }

    @GetMapping("/teeing")
    public StreamDemoResponse<ModernStreamOperationsService.SalarySummary> teeing() {
        return StreamDemoResponse.<ModernStreamOperationsService.SalarySummary>builder()
                .operation("Collectors.teeing — Java 12")
                .description("Runs TWO collectors over the SAME single pass of the stream, merging their results at the end. Before Java 12, getting an average AND a count together meant either two full traversals, or reaching for the heavier summarizingDouble.")
                .codeSnippet("employees.stream().collect(teeing(averagingDouble(Employee::salary), counting(), SalarySummary::new))")
                .result(service.teeingAverageAndCount())
                .build();
    }

    @GetMapping("/ofnullable")
    public StreamDemoResponse<List<Employee>> ofNullable(
            @RequestParam(defaultValue = "1,2,999,15") List<Long> ids) {
        return StreamDemoResponse.<List<Employee>>builder()
                .operation("Stream.ofNullable — Java 9")
                .description("Builds a stream of ZERO or ONE element depending on whether the value is null — its natural home is inside flatMap, silently skipping ids that resolve to nothing (999 here doesn't exist) with no explicit null-check branch needed.")
                .codeSnippet("ids.stream().flatMap(id -> Stream.ofNullable(lookup(id)))")
                .result(service.ofNullableLookup(ids))
                .build();
    }

    @GetMapping("/optional-stream")
    public StreamDemoResponse<List<Employee>> optionalStream(
            @RequestParam(defaultValue = "1,2,999,15") List<Long> ids) {
        return StreamDemoResponse.<List<Employee>>builder()
                .operation("Optional.stream() — Java 9")
                .description("The reverse direction of ofNullable: turns an Optional<T> into a zero-or-one-element Stream, so a stream of independent Optional-returning lookups flattens straight into present values via flatMap.")
                .codeSnippet("ids.stream().map(this::lookup).flatMap(Optional::stream)")
                .result(service.optionalStreamLookup(ids))
                .build();
    }

    @GetMapping("/mapmulti")
    public StreamDemoResponse<List<String>> mapMulti() {
        return StreamDemoResponse.<List<String>>builder()
                .operation("Stream.mapMulti — Java 16")
                .description("An alternative to flatMap for the 'each element maps to 0..n values' case, WITHOUT allocating an intermediate Stream per element — values are pushed into a consumer instead. Compare against /streams/intermediate/flatmap, which solves the exact same problem the Java 8 way.")
                .codeSnippet("employees.stream().<String>mapMulti((e, consumer) -> { consumer.accept(e.department()); consumer.accept(e.city()); })")
                .result(service.mapMultiTags())
                .build();
    }

    @GetMapping("/filtering")
    public StreamDemoResponse<Map<String, List<Employee>>> filtering(
            @RequestParam(defaultValue = "100000") double salaryThreshold) {
        return StreamDemoResponse.<Map<String, List<Employee>>>builder()
                .operation("Collectors.filtering — Java 9")
                .description("Filters WITHIN a downstream collector, most useful inside groupingBy. A plain filter() applied BEFORE groupingBy would silently drop whole department keys with zero matches; filtering() keeps every key present, just with a (possibly empty) filtered list.")
                .codeSnippet("employees.stream().collect(groupingBy(Employee::department, filtering(e -> e.salary() > " + salaryThreshold + ", toList())))")
                .result(service.filteringWithinGroups(salaryThreshold))
                .build();
    }

    @GetMapping("/iterate-predicate")
    public StreamDemoResponse<List<Integer>> iteratePredicate(
            @RequestParam(defaultValue = "1") int seed,
            @RequestParam(defaultValue = "1000") @Min(1) @Max(1_000_000) int bound) {
        return StreamDemoResponse.<List<Integer>>builder()
                .operation("Stream.iterate(seed, hasNext, next) — Java 9")
                .description("A three-argument iterate overload with an explicit continuation predicate, making the stream naturally finite. The Java 8 idiom, iterate(seed, next).limit(n), needed a separate element count because Java 8's two-argument iterate is always infinite.")
                .codeSnippet("Stream.iterate(" + seed + ", n -> n <= " + bound + ", n -> n * 2)")
                .result(service.iterateWithPredicate(seed, bound))
                .build();
    }
}