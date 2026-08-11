package com.dockyard.javastreams.controller;

import com.dockyard.javastreams.domain.Employee;
import com.dockyard.javastreams.dto.StreamDemoResponse;
import com.dockyard.javastreams.service.IntermediateOperationsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * IntermediateOperationsController — operations that transform a stream into
 * another stream (lazy — see {@link IntermediateOperationsService}).
 */
@RestController
@RequestMapping("/streams/intermediate")
@RequiredArgsConstructor
@Validated
public class IntermediateOperationsController {

    private final IntermediateOperationsService service;

    @GetMapping("/filter")
    public StreamDemoResponse<List<Employee>> filter(@RequestParam(defaultValue = "Engineering") String department) {
        return StreamDemoResponse.<List<Employee>>builder()
                .operation("filter")
                .description("Keeps only elements matching a Predicate. Stateless — each element is judged independently.")
                .codeSnippet("employees.stream().filter(e -> e.department().equals(\"" + department + "\"))")
                .result(service.filterByDepartment(department))
                .build();
    }

    @GetMapping("/map")
    public StreamDemoResponse<List<String>> map() {
        return StreamDemoResponse.<List<String>>builder()
                .operation("map")
                .description("Transforms each element 1-to-1 into something else. Stateless.")
                .codeSnippet("employees.stream().map(Employee::name)")
                .result(service.mapToNames())
                .build();
    }

    @GetMapping("/flatmap")
    public StreamDemoResponse<List<String>> flatMap() {
        return StreamDemoResponse.<List<String>>builder()
                .operation("flatMap")
                .description("Maps each element to its OWN stream, then flattens all of them into one stream. Needed whenever map() would otherwise produce a stream-of-streams.")
                .codeSnippet("employees.stream().flatMap(e -> Stream.of(e.department(), e.city())).distinct()")
                .result(service.flatMapSkillTags())
                .build();
    }

    @GetMapping("/distinct")
    public StreamDemoResponse<List<String>> distinct() {
        return StreamDemoResponse.<List<String>>builder()
                .operation("distinct")
                .description("Removes duplicates via equals()/hashCode(). Stateful — must remember every value seen so far.")
                .codeSnippet("employees.stream().map(Employee::department).distinct()")
                .result(service.distinctDepartments())
                .build();
    }

    @GetMapping("/sorted")
    public StreamDemoResponse<List<String>> sorted() {
        return StreamDemoResponse.<List<String>>builder()
                .operation("sorted (natural ordering)")
                .description("Sorts using the elements' natural Comparable ordering. Stateful — must see the whole stream before producing the first output element.")
                .codeSnippet("employees.stream().map(Employee::name).sorted()")
                .result(service.sortedNames())
                .build();
    }

    @GetMapping("/sorted-comparator")
    public StreamDemoResponse<List<Employee>> sortedComparator() {
        return StreamDemoResponse.<List<Employee>>builder()
                .operation("sorted (Comparator, multi-key)")
                .description("Comparator.comparing(...).reversed().thenComparing(...) composes a multi-key custom sort readably.")
                .codeSnippet("employees.stream().sorted(Comparator.comparingDouble(Employee::salary).reversed().thenComparing(Employee::name))")
                .result(service.sortedBySalaryDesc())
                .build();
    }

    public record PeekResult(List<String> names, List<String> executionTrace) {
    }

    @GetMapping("/peek")
    public StreamDemoResponse<PeekResult> peek() {
        List<String> trace = new ArrayList<>();
        List<String> names = service.peekTrace(trace);
        return StreamDemoResponse.<PeekResult>builder()
                .operation("peek")
                .description("Runs a side-effecting action on each element as it passes through, WITHOUT changing the stream. Intended for debugging a pipeline, not for real work — 'executionTrace' shows exactly when peek fired relative to filter/map, ELEMENT BY ELEMENT (a stream processes one element through the WHOLE pipeline before starting the next — it does not run filter over everything, then map over everything).")
                .codeSnippet("employees.stream().filter(...).peek(e -> trace.add(...)).map(...).peek(e -> trace.add(...))")
                .result(new PeekResult(names, trace))
                .build();
    }

    @GetMapping("/limit")
    public StreamDemoResponse<List<Employee>> limit(@RequestParam(defaultValue = "3") @Min(1) @Max(20) long count) {
        return StreamDemoResponse.<List<Employee>>builder()
                .operation("limit")
                .description("Truncates the stream to at most N elements. Short-circuiting — the source doesn't need to be fully known ahead of time.")
                .codeSnippet("employees.stream().sorted(bySalaryDesc).limit(" + count + ")")
                .result(service.topEarners(count))
                .build();
    }

    @GetMapping("/skip")
    public StreamDemoResponse<List<Employee>> skip(@RequestParam(defaultValue = "3") @Min(0) @Max(20) long count) {
        return StreamDemoResponse.<List<Employee>>builder()
                .operation("skip")
                .description("Discards the first N elements, keeping the rest. Must still traverse the skipped elements internally.")
                .codeSnippet("employees.stream().sorted(bySalaryDesc).skip(" + count + ")")
                .result(service.allButTopEarners(count))
                .build();
    }
}