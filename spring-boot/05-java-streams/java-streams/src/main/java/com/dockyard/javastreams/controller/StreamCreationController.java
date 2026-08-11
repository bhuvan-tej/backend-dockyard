package com.dockyard.javastreams.controller;

import com.dockyard.javastreams.dto.StreamDemoResponse;
import com.dockyard.javastreams.service.StreamCreationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * StreamCreationController — every way of OBTAINING a Stream (see
 * {@link StreamCreationService} for the detailed "why" behind each one).
 */
@RestController
@RequestMapping("/streams/creation")
@RequiredArgsConstructor
@Validated
public class StreamCreationController {

    private final StreamCreationService service;

    @GetMapping("/collection")
    public StreamDemoResponse<List<String>> fromCollection() {
        List<String> input = List.of("stream", "map", "filter", "reduce", "collect");
        return StreamDemoResponse.<List<String>>builder()
                .operation("Stream creation: Collection.stream()")
                .description("The most common source: calling .stream() on any existing List/Set/Collection.")
                .codeSnippet("source.stream().map(String::toUpperCase).collect(Collectors.toList())")
                .result(service.fromCollection(input))
                .build();
    }

    @GetMapping("/varargs")
    public StreamDemoResponse<List<String>> fromVarargs() {
        return StreamDemoResponse.<List<String>>builder()
                .operation("Stream creation: Stream.of(varargs)")
                .description("A fixed, inline sequence of literal values — no collection needed at all.")
                .codeSnippet("Stream.of(\"stream\", \"map\", \"filter\", \"reduce\", \"collect\")")
                .result(service.fromVarargs())
                .build();
    }

    @GetMapping("/array")
    public StreamDemoResponse<List<Integer>> fromArray() {
        return StreamDemoResponse.<List<Integer>>builder()
                .operation("Stream creation: Arrays.stream(array)")
                .description("Any array — primitive or object — can be turned into a stream directly.")
                .codeSnippet("Arrays.stream(new int[]{5,3,8,1,9,2}).boxed().sorted()")
                .result(service.fromArray())
                .build();
    }

    @GetMapping("/int-range")
    public StreamDemoResponse<List<Integer>> fromIntRange(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int end) {
        return StreamDemoResponse.<List<Integer>>builder()
                .operation("Stream creation: IntStream.range(start, end)")
                .description("The Stream-based replacement for a classic counting for-loop. 'range' excludes the upper bound.")
                .codeSnippet("IntStream.range(" + start + ", " + end + ").boxed()")
                .result(service.fromIntRange(start, end))
                .build();
    }

    @GetMapping("/generate")
    public StreamDemoResponse<List<Integer>> fromGenerate(
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int count) {
        return StreamDemoResponse.<List<Integer>>builder()
                .operation("Stream creation: Stream.generate(Supplier)")
                .description("An INFINITE stream where each element is produced independently by a Supplier — must be bounded with limit().")
                .codeSnippet("Stream.generate(supplier).limit(" + count + ")")
                .result(service.fromGenerate(count))
                .build();
    }

    @GetMapping("/iterate")
    public StreamDemoResponse<List<Integer>> fromIterate(
            @RequestParam(defaultValue = "1") int seed,
            @RequestParam(defaultValue = "8") @Min(1) @Max(100) int count) {
        return StreamDemoResponse.<List<Integer>>builder()
                .operation("Stream creation: Stream.iterate(seed, UnaryOperator)")
                .description("An INFINITE stream where each element derives from the PREVIOUS one — also must be bounded with limit().")
                .codeSnippet("Stream.iterate(" + seed + ", n -> n * 2).limit(" + count + ")")
                .result(service.fromIterate(seed, count))
                .build();
    }

    @GetMapping("/empty")
    public StreamDemoResponse<List<String>> empty() {
        return StreamDemoResponse.<List<String>>builder()
                .operation("Stream creation: Stream.empty()")
                .description("A valid, zero-element stream — terminates immediately. Useful as a base case in conditional logic.")
                .codeSnippet("Stream.<String>empty().collect(Collectors.toList())")
                .result(service.empty())
                .build();
    }

    @GetMapping("/concat")
    public StreamDemoResponse<List<String>> concatenated() {
        return StreamDemoResponse.<List<String>>builder()
                .operation("Stream creation: Stream.concat(a, b)")
                .description("Glues two streams together into one, preserving the order of each source.")
                .codeSnippet("Stream.concat(Stream.of(\"a\",\"b\",\"c\"), Stream.of(\"x\",\"y\",\"z\"))")
                .result(service.concatenated())
                .build();
    }
}