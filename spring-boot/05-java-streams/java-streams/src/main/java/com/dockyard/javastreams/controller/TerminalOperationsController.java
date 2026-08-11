package com.dockyard.javastreams.controller;

import com.dockyard.javastreams.domain.Employee;
import com.dockyard.javastreams.dto.StreamDemoResponse;
import com.dockyard.javastreams.service.TerminalOperationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * TerminalOperationsController — the operations that actually EXECUTE a
 * pipeline (see {@link TerminalOperationsService} for the lazy-vs-eager and
 * short-circuiting details).
 */
@RestController
@RequestMapping("/streams/terminal")
@RequiredArgsConstructor
public class TerminalOperationsController {

    private final TerminalOperationsService service;

    @GetMapping("/foreach")
    public StreamDemoResponse<List<String>> forEach() {
        return StreamDemoResponse.<List<String>>builder()
                .operation("forEach")
                .description("Runs a side-effecting action per element; returns nothing itself. NOT short-circuiting — visits every element.")
                .codeSnippet("employees.stream().filter(isEngineering).forEach(e -> collected.add(e.name()))")
                .result(service.forEachCollectNames())
                .build();
    }

    @GetMapping("/count")
    public StreamDemoResponse<Long> count(@RequestParam(defaultValue = "Engineering") String department) {
        return StreamDemoResponse.<Long>builder()
                .operation("count")
                .description("Visits every element and returns how many matched. NOT short-circuiting, even though the answer is just a number.")
                .codeSnippet("employees.stream().filter(e -> e.department().equals(\"" + department + "\")).count()")
                .result(service.countInDepartment(department))
                .build();
    }

    @GetMapping("/max")
    public StreamDemoResponse<Optional<Employee>> max() {
        return StreamDemoResponse.<Optional<Employee>>builder()
                .operation("max(Comparator)")
                .description("Needs a Comparator (there's no 'natural max' for an arbitrary object). Returns Optional because an empty stream has no maximum.")
                .codeSnippet("employees.stream().max(Comparator.comparingDouble(Employee::salary))")
                .result(service.highestPaid())
                .build();
    }

    @GetMapping("/min")
    public StreamDemoResponse<Optional<Employee>> min() {
        return StreamDemoResponse.<Optional<Employee>>builder()
                .operation("min(Comparator)")
                .description("Same idea as max, inverted.")
                .codeSnippet("employees.stream().min(Comparator.comparingDouble(Employee::salary))")
                .result(service.lowestPaid())
                .build();
    }

    @GetMapping("/anymatch")
    public StreamDemoResponse<Boolean> anyMatch(@RequestParam(defaultValue = "100000") double threshold) {
        return StreamDemoResponse.<Boolean>builder()
                .operation("anyMatch")
                .description("Short-circuits at the FIRST element satisfying the predicate — does not necessarily visit every element.")
                .codeSnippet("employees.stream().anyMatch(e -> e.salary() > " + threshold + ")")
                .result(service.anyoneEarnsOver(threshold))
                .build();
    }

    @GetMapping("/allmatch")
    public StreamDemoResponse<Boolean> allMatch(@RequestParam(defaultValue = "18") int minAge) {
        return StreamDemoResponse.<Boolean>builder()
                .operation("allMatch")
                .description("Short-circuits at the FIRST element that FAILS the predicate.")
                .codeSnippet("employees.stream().allMatch(e -> e.age() >= " + minAge + ")")
                .result(service.everyoneIsAdult(minAge))
                .build();
    }

    @GetMapping("/nonematch")
    public StreamDemoResponse<Boolean> noneMatch(@RequestParam(defaultValue = "50000") double floor) {
        return StreamDemoResponse.<Boolean>builder()
                .operation("noneMatch")
                .description("Short-circuits at the FIRST element that satisfies the predicate (i.e. the first counter-example).")
                .codeSnippet("employees.stream().noneMatch(e -> e.salary() < " + floor + ")")
                .result(service.noOneUnderpaid(floor))
                .build();
    }

    @GetMapping("/findfirst")
    public StreamDemoResponse<Optional<Employee>> findFirst(@RequestParam(defaultValue = "Sales") String department) {
        return StreamDemoResponse.<Optional<Employee>>builder()
                .operation("findFirst")
                .description("The first matching element in ENCOUNTER order. Short-circuits as soon as one match is found.")
                .codeSnippet("employees.stream().filter(e -> e.department().equals(\"" + department + "\")).findFirst()")
                .result(service.firstInDepartment(department))
                .build();
    }

    @GetMapping("/findany")
    public StreamDemoResponse<Optional<Employee>> findAny(@RequestParam(defaultValue = "Mumbai") String city) {
        return StreamDemoResponse.<Optional<Employee>>builder()
                .operation("findAny")
                .description("ANY matching element, not necessarily the first — matters most on PARALLEL streams, where 'first' is expensive to guarantee. On a sequential stream it usually behaves like findFirst.")
                .codeSnippet("employees.stream().filter(e -> e.city().equals(\"" + city + "\")).findAny()")
                .result(service.anyInCity(city))
                .build();
    }

    @GetMapping("/reduce-no-identity")
    public StreamDemoResponse<Optional<Double>> reduceNoIdentity() {
        return StreamDemoResponse.<Optional<Double>>builder()
                .operation("reduce(BinaryOperator) — no identity")
                .description("Returns Optional because an EMPTY stream has no way to produce a result without a starting value.")
                .codeSnippet("employees.stream().map(Employee::salary).reduce(Double::max)")
                .result(service.reduceMaxSalaryNoIdentity())
                .build();
    }

    @GetMapping("/reduce-identity")
    public StreamDemoResponse<Double> reduceWithIdentity() {
        return StreamDemoResponse.<Double>builder()
                .operation("reduce(identity, BinaryOperator)")
                .description("With an explicit seed/identity value, an empty stream simply returns that identity — no Optional needed.")
                .codeSnippet("employees.stream().map(Employee::salary).reduce(0.0, Double::sum)")
                .result(service.reduceTotalSalary())
                .build();
    }

    @GetMapping("/reduce-combiner")
    public StreamDemoResponse<Integer> reduceWithCombiner(@RequestParam(defaultValue = "100000") double threshold) {
        return StreamDemoResponse.<Integer>builder()
                .operation("reduce(identity, accumulator, combiner)")
                .description("Needed when the running result type differs from the stream's element type (here: Employee -> int count). The COMBINER merges partial results from different chunks — required for correctness on PARALLEL streams, even though a sequential stream never calls it.")
                .codeSnippet("employees.stream().reduce(0, (count, e) -> e.salary() > " + threshold + " ? count + 1 : count, Integer::sum)")
                .result(service.reduceCountAboveSalary(threshold))
                .build();
    }
}