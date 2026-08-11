package com.dockyard.javastreams.service;

import com.dockyard.javastreams.dataset.EmployeeDataset;
import com.dockyard.javastreams.domain.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ModernStreamOperationsService — everything the Stream/Collectors API
 * gained AFTER Java 8 (Java 9 through Java 16), kept in its own class on
 * purpose. Every OTHER service in this app is deliberately restricted to
 * Java 8-era methods only; this is the single place where "what changed
 * since, and why does it matter" lives, with each method paired against its
 * Java 8-era equivalent for direct comparison. See LEARNING.md section 9
 * for the full write-up of why the Java-8-only restriction exists at all.
 */
@Service
@RequiredArgsConstructor
public class ModernStreamOperationsService {

    private final EmployeeDataset dataset;

    /** teeing()'s merge target — computing both in one pass instead of two separate stream traversals. */
    public record SalarySummary(double averageSalary, long count) {
    }

    /**
     * Stream.toList() (Java 16) — shorthand for
     * {@code collect(Collectors.toUnmodifiableList())}. Two differences from
     * the Java 8 {@code collect(Collectors.toList())}: it's shorter, AND the
     * list it returns is UNMODIFIABLE — calling add()/remove() on the result
     * throws UnsupportedOperationException.
     */
    public List<String> toListShorthand() {
        return dataset.all().stream()
                .map(Employee::name)
                .toList();
    }

    /**
     * Stream.takeWhile(Predicate) (Java 9) — takes elements from the FRONT
     * of an ORDERED stream while the predicate holds, then STOPS at the
     * first element that fails it, even if a later element would have
     * matched. Only meaningful on an ordered/sorted source — contrast with
     * {@code filter}, which independently judges every element regardless
     * of position.
     */
    public List<Employee> takeWhileJoinedBefore(int year) {
        return dataset.all().stream()
                .sorted(Comparator.comparingInt(Employee::joiningYear))
                .takeWhile(e -> e.joiningYear() < year)
                .collect(Collectors.toList());
    }

    /**
     * Stream.dropWhile(Predicate) (Java 9) — the mirror image of
     * takeWhile: DISCARDS elements from the front while the predicate
     * holds, then keeps everything after that point regardless of whether
     * it still matches.
     */
    public List<Employee> dropWhileJoinedBefore(int year) {
        return dataset.all().stream()
                .sorted(Comparator.comparingInt(Employee::joiningYear))
                .dropWhile(e -> e.joiningYear() < year)
                .collect(Collectors.toList());
    }

    /**
     * Collectors.teeing(downstream1, downstream2, merger) (Java 12) — runs
     * TWO collectors over the SAME single pass of the stream, then merges
     * their two results at the end via the merge function. Before Java 12,
     * getting an average AND a count together meant either two separate
     * stream traversals, or reaching for the heavier
     * {@code summarizingDouble} and pulling two fields back out of it.
     */
    public SalarySummary teeingAverageAndCount() {
        return dataset.all().stream()
                .collect(Collectors.teeing(
                        Collectors.averagingDouble(Employee::salary),
                        Collectors.counting(),
                        SalarySummary::new));
    }

    /**
     * Stream.ofNullable(value) (Java 9) — builds a stream of ZERO or ONE
     * element depending on whether the value is null. Its natural home is
     * inside a flatMap: {@code ids.stream().flatMap(id -> Stream.ofNullable(lookup(id)))}
     * silently skips ids that resolve to nothing, with no explicit
     * null-check branch needed.
     */
    public List<Employee> ofNullableLookup(List<Long> ids) {
        return ids.stream()
                .flatMap(id -> Stream.ofNullable(dataset.findById(id).orElse(null)))
                .collect(Collectors.toList());
    }

    /**
     * Optional.stream() (Java 9) — the reverse direction of ofNullable:
     * turns an {@code Optional<T>} into a zero-or-one-element Stream, so a
     * stream of independent Optional-returning lookups can be flattened
     * straight into a stream of present values via flatMap, with absent
     * results simply vanishing instead of needing to be filtered out.
     */
    public List<Employee> optionalStreamLookup(List<Long> ids) {
        return ids.stream()
                .map(dataset::findById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    /**
     * Stream.mapMulti (Java 16) — an alternative to flatMap for the "each
     * element maps to 0..n values" case, WITHOUT allocating an
     * intermediate Stream object per element — values are pushed into a
     * consumer instead. Compare against {@code IntermediateOperationsService.flatMapSkillTags},
     * which solves the exact same "one employee, several tags" problem the
     * Java 8 way.
     */
    public List<String> mapMultiTags() {
        return dataset.all().stream()
                .<String>mapMulti((employee, consumer) -> {
                    consumer.accept(employee.department());
                    consumer.accept(employee.city());
                })
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Collectors.filtering(predicate, downstream) (Java 9) — filters
     * WITHIN a downstream collector, most useful paired with groupingBy. A
     * plain {@code stream().filter(...)} applied BEFORE groupingBy would
     * silently drop entire department keys that end up with zero matches;
     * filtering() keeps every department key present, just with a
     * (possibly empty) filtered list underneath it.
     */
    public Map<String, List<Employee>> filteringWithinGroups(double salaryThreshold) {
        return dataset.all().stream()
                .collect(Collectors.groupingBy(
                        Employee::department,
                        Collectors.filtering(e -> e.salary() > salaryThreshold, Collectors.toList())));
    }

    /**
     * Stream.iterate(seed, hasNext, next) (Java 9) — a three-argument
     * overload with an explicit continuation predicate, making the stream
     * naturally FINITE. The classic Java 8 idiom,
     * {@code Stream.iterate(seed, next).limit(n)}, needed a separate
     * element count because Java 8's two-argument iterate is always
     * infinite and has no idea when to stop on its own.
     */
    public List<Integer> iterateWithPredicate(int seed, int bound) {
        return Stream.iterate(seed, n -> n <= bound, n -> n * 2)
                .collect(Collectors.toList());
    }
}