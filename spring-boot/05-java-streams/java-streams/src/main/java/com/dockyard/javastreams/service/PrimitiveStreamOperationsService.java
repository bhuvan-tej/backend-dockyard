package com.dockyard.javastreams.service;

import com.dockyard.javastreams.dataset.EmployeeDataset;
import com.dockyard.javastreams.domain.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * PrimitiveStreamOperationsService — {@code IntStream}/{@code LongStream}/
 * {@code DoubleStream} exist for one reason: a {@code Stream<Integer>}
 * secretly boxes every {@code int} into an {@code Integer} object, which is
 * wasted allocation and indirection for pure numeric work. {@code mapToInt}/
 * {@code mapToDouble}/{@code mapToLong} switch a regular object Stream onto
 * one of these specialized primitive streams, which come with their OWN
 * terminal operations ({@code sum}, {@code average}, {@code max}, {@code min},
 * {@code summaryStatistics}) that a plain {@code Stream<T>} simply doesn't
 * have — because "sum of a stream of Employees" is meaningless, but "sum of
 * a stream of ints" isn't. {@code .boxed()} converts back to the object
 * Stream world (e.g. to put results in a {@code List<Integer>}) once the
 * numeric work is done.
 */
@Service
@RequiredArgsConstructor
public class PrimitiveStreamOperationsService {

    private final EmployeeDataset dataset;

    /**
     * mapToInt — switches a {@code Stream<Employee>} onto an
     * {@code IntStream} of ages, then uses {@code sum()}, one of the
     * terminal operations only a primitive stream has.
     */
    public int totalAgeYears() {
        return dataset.all().stream()
                .mapToInt(Employee::age)
                .sum();
    }

    /**
     * IntStream.average() — returns {@code OptionalDouble} (a primitive
     * specialization of {@code Optional}, avoiding boxing even for the
     * "might be empty" wrapper itself), because an empty stream has no
     * average to report.
     */
    public OptionalDouble averageAge() {
        return dataset.all().stream()
                .mapToInt(Employee::age)
                .average();
    }

    /** IntStream.max()/min() — return {@code OptionalInt}, mirroring {@code Stream.max}/{@code min} but unboxed. */
    public OptionalInt oldestAge() {
        return dataset.all().stream()
                .mapToInt(Employee::age)
                .max();
    }

    /**
     * IntStream.summaryStatistics() — one pass, ALL five numbers at once
     * (count, sum, min, max, average) as a plain {@code IntSummaryStatistics}
     * value object — the primitive-stream sibling of
     * {@code Collectors.summarizingInt}.
     */
    public IntSummaryStatistics ageStatistics() {
        return dataset.all().stream()
                .mapToInt(Employee::age)
                .summaryStatistics();
    }

    /** mapToDouble — the same idea as mapToInt, for a {@code double}-valued field (salary). */
    public DoubleSummaryStatistics salaryStatisticsViaPrimitiveStream() {
        return dataset.all().stream()
                .mapToDouble(Employee::salary)
                .summaryStatistics();
    }

    /**
     * IntStream.boxed() — converts BACK from {@code IntStream} to
     * {@code Stream<Integer>}, needed the moment you want to put the values
     * in a generic collection like {@code List<Integer>} (a {@code List}
     * can't hold raw primitives — only their boxed wrapper type).
     */
    public List<Integer> sortedUniqueAges() {
        return dataset.all().stream()
                .mapToInt(Employee::age)
                .distinct()
                .sorted()
                .boxed()
                .collect(Collectors.toList());
    }

    /**
     * IntStream.rangeClosed + filter — a purely numeric pipeline with no
     * object stream involved at all, to show IntStream working entirely on
     * its own terms (e.g. classic "sum of even numbers" style problems),
     * not just as something you arrive at via {@code mapToInt}.
     */
    public int sumOfEvenNumbersUpTo(int bound) {
        return IntStream.rangeClosed(1, bound)
                .filter(n -> n % 2 == 0)
                .sum();
    }
}