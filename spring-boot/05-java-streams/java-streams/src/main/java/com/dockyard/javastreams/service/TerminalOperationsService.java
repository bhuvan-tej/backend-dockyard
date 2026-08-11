package com.dockyard.javastreams.service;

import com.dockyard.javastreams.dataset.EmployeeDataset;
import com.dockyard.javastreams.domain.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TerminalOperationsService — the operations that actually TRIGGER a
 * pipeline to run. Every method above in {@code IntermediateOperationsService}
 * is lazy and does nothing on its own; one of these is always what makes a
 * stream pipeline actually execute.
 *
 * A useful second axis: SHORT-CIRCUITING terminal operations
 * ({@code anyMatch}, {@code findFirst}, {@code findAny}) can stop as soon as
 * they have an answer, without visiting the rest of the stream. Others
 * ({@code count}, {@code forEach}, a plain {@code collect}) must visit every
 * element no matter what.
 */
@Service
@RequiredArgsConstructor
public class TerminalOperationsService {

    private final EmployeeDataset dataset;

    /** forEach — runs a side-effecting action per element; returns nothing. Not short-circuiting. */
    public List<String> forEachCollectNames() {
        List<String> collected = new java.util.ArrayList<>();
        dataset.all().stream()
                .filter(e -> e.department().equals("Engineering"))
                .forEach(e -> collected.add(e.name())); // side effect — fine here because 'collected' is local & not shared across threads
        return collected;
    }

    /** count — visits every element, returns how many matched. Not short-circuiting. */
    public long countInDepartment(String department) {
        return dataset.all().stream()
                .filter(e -> e.department().equalsIgnoreCase(department))
                .count();
    }

    /** min/max — need a Comparator, return Optional (empty stream = no min/max). */
    public Optional<Employee> highestPaid() {
        return dataset.all().stream()
                .max(Comparator.comparingDouble(Employee::salary));
    }

    public Optional<Employee> lowestPaid() {
        return dataset.all().stream()
                .min(Comparator.comparingDouble(Employee::salary));
    }

    /** anyMatch — short-circuits at the FIRST element satisfying the predicate. */
    public boolean anyoneEarnsOver(double threshold) {
        return dataset.all().stream()
                .anyMatch(e -> e.salary() > threshold);
    }

    /** allMatch — short-circuits at the FIRST element that FAILS the predicate. */
    public boolean everyoneIsAdult(int minAge) {
        return dataset.all().stream()
                .allMatch(e -> e.age() >= minAge);
    }

    /** noneMatch — short-circuits at the FIRST element that satisfies the predicate. */
    public boolean noOneUnderpaid(double floor) {
        return dataset.all().stream()
                .noneMatch(e -> e.salary() < floor);
    }

    /** findFirst — the first matching element in ENCOUNTER order; short-circuits immediately once found. */
    public Optional<Employee> firstInDepartment(String department) {
        return dataset.all().stream()
                .filter(e -> e.department().equalsIgnoreCase(department))
                .findFirst();
    }

    /** findAny — ANY matching element, not necessarily the first; on a sequential stream, usually behaves like findFirst. */
    public Optional<Employee> anyInCity(String city) {
        return dataset.all().stream()
                .filter(e -> e.city().equalsIgnoreCase(city))
                .findAny();
    }

    /**
     * reduce(BinaryOperator) — the NO-IDENTITY overload. Returns
     * {@code Optional} because an empty stream has no way to produce a
     * result without a starting value.
     */
    public Optional<Double> reduceMaxSalaryNoIdentity() {
        return dataset.all().stream()
                .map(Employee::salary)
                .reduce(Double::max);
    }

    /**
     * reduce(identity, BinaryOperator) — the two-argument overload. With an
     * explicit identity/seed value, an empty stream simply returns that
     * identity — no {@code Optional} needed.
     */
    public double reduceTotalSalary() {
        return dataset.all().stream()
                .map(Employee::salary)
                .reduce(0.0, Double::sum);
    }

    /**
     * reduce(identity, accumulator, combiner) — the three-argument overload,
     * needed when the stream's element type differs from the running result
     * type (here: Employee -> running int count). The COMBINER is what makes
     * this reducible in PARALLEL: it merges two partial results computed on
     * different chunks of the stream. On a sequential stream the combiner is
     * simply never invoked, but it must still be supplied and be correct.
     */
    public int reduceCountAboveSalary(double threshold) {
        return dataset.all().stream()
                .reduce(0,
                        (partialCount, employee) -> employee.salary() > threshold ? partialCount + 1 : partialCount,
                        Integer::sum);
    }

    /** A tiny illustration of why forEach + shared mutable state needs care — see LEARNING.md. */
    public int forEachSideEffectCount() {
        AtomicInteger counter = new AtomicInteger(0); // thread-safe counter, not a plain int, on purpose
        dataset.all().stream()
                .forEach(e -> counter.incrementAndGet());
        return counter.get();
    }
}