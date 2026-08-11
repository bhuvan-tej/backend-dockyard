package com.dockyard.javastreams.controller;

import com.dockyard.javastreams.dto.StreamDemoResponse;
import com.dockyard.javastreams.service.PrimitiveStreamOperationsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * PrimitiveStreamOperationsController — {@code IntStream}/{@code DoubleStream}
 * and the {@code mapToInt}/{@code mapToDouble}/{@code boxed} bridge between
 * them and a regular object {@code Stream} (see
 * {@link PrimitiveStreamOperationsService} for the detailed "why").
 */
@RestController
@RequestMapping("/streams/primitives")
@RequiredArgsConstructor
@Validated
public class PrimitiveStreamOperationsController {

    private final PrimitiveStreamOperationsService service;

    @GetMapping("/maptoint-sum")
    public StreamDemoResponse<Integer> mapToIntSum() {
        return StreamDemoResponse.<Integer>builder()
                .operation("Stream.mapToInt(fn).sum()")
                .description("Switches Stream<Employee> onto an IntStream of ages, then calls sum() — a terminal operation only a PRIMITIVE stream has, because 'sum of Employees' is meaningless but 'sum of ints' isn't.")
                .codeSnippet("employees.stream().mapToInt(Employee::age).sum()")
                .result(service.totalAgeYears())
                .build();
    }

    @GetMapping("/maptoint-average")
    public StreamDemoResponse<OptionalDouble> mapToIntAverage() {
        return StreamDemoResponse.<OptionalDouble>builder()
                .operation("IntStream.average()")
                .description("Returns OptionalDouble — a primitive specialization of Optional avoiding boxing even for the wrapper itself — because an empty stream has no average to report.")
                .codeSnippet("employees.stream().mapToInt(Employee::age).average()")
                .result(service.averageAge())
                .build();
    }

    @GetMapping("/maptoint-max")
    public StreamDemoResponse<OptionalInt> mapToIntMax() {
        return StreamDemoResponse.<OptionalInt>builder()
                .operation("IntStream.max()")
                .description("Mirrors Stream.max/min but returns OptionalInt instead of Optional<Integer> — no boxing.")
                .codeSnippet("employees.stream().mapToInt(Employee::age).max()")
                .result(service.oldestAge())
                .build();
    }

    @GetMapping("/summary-stats-int")
    public StreamDemoResponse<IntSummaryStatistics> summaryStatsInt() {
        return StreamDemoResponse.<IntSummaryStatistics>builder()
                .operation("IntStream.summaryStatistics()")
                .description("One pass, all five numbers at once (count/sum/min/max/average) — the primitive-stream sibling of Collectors.summarizingInt.")
                .codeSnippet("employees.stream().mapToInt(Employee::age).summaryStatistics()")
                .result(service.ageStatistics())
                .build();
    }

    @GetMapping("/summary-stats-double")
    public StreamDemoResponse<DoubleSummaryStatistics> summaryStatsDouble() {
        return StreamDemoResponse.<DoubleSummaryStatistics>builder()
                .operation("DoubleStream.summaryStatistics() (via mapToDouble)")
                .description("The same idea as mapToInt, applied to a double-valued field (salary) instead of an int-valued one.")
                .codeSnippet("employees.stream().mapToDouble(Employee::salary).summaryStatistics()")
                .result(service.salaryStatisticsViaPrimitiveStream())
                .build();
    }

    @GetMapping("/boxed")
    public StreamDemoResponse<List<Integer>> boxed() {
        return StreamDemoResponse.<List<Integer>>builder()
                .operation("IntStream.boxed()")
                .description("Converts BACK from IntStream to Stream<Integer> — needed the moment you want the values in a generic collection like List<Integer>, since a List can't hold raw primitives.")
                .codeSnippet("employees.stream().mapToInt(Employee::age).distinct().sorted().boxed().collect(Collectors.toList())")
                .result(service.sortedUniqueAges())
                .build();
    }

    @GetMapping("/rangeclosed-sum")
    public StreamDemoResponse<Integer> rangeClosedSum(
            @RequestParam(defaultValue = "20") @Min(1) @Max(10_000) int bound) {
        return StreamDemoResponse.<Integer>builder()
                .operation("IntStream.rangeClosed(1, n).filter(...).sum()")
                .description("A purely numeric pipeline with no object Stream involved at all — IntStream working entirely on its own terms, not just as something arrived at via mapToInt.")
                .codeSnippet("IntStream.rangeClosed(1, " + bound + ").filter(n -> n % 2 == 0).sum()")
                .result(service.sumOfEvenNumbersUpTo(bound))
                .build();
    }
}