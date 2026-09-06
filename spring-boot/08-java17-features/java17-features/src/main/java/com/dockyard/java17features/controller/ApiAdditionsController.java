package com.dockyard.java17features.controller;

import com.dockyard.java17features.dto.Java17DemoResponse;
import com.dockyard.java17features.dto.StringHelperResult;
import com.dockyard.java17features.dto.TeeingResult;
import com.dockyard.java17features.dto.ToListResult;
import com.dockyard.java17features.service.ApiAdditionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ApiAdditionsController — the library-level additions from Java 12–16. See
 * {@link ApiAdditionsService} for the detailed "why".
 */
@RestController
@RequestMapping("/java17/api")
@RequiredArgsConstructor
@Validated
public class ApiAdditionsController {

    private final ApiAdditionsService service;

    @GetMapping("/stream-to-list")
    public Java17DemoResponse<ToListResult> streamToList() {
        return Java17DemoResponse.<ToListResult>builder()
                .operation("Stream.toList() (Java 16) vs collect(Collectors.toList())")
                .description("Terser to write, but the real difference is the CONTRACT: toList() returns an UNMODIFIABLE list, while Collectors.toList() guarantees nothing about type or mutability (it happens to give you an ArrayList). toList() is not toUnmodifiableList() either — it tolerates null elements, which toUnmodifiableList() rejects.")
                .codeSnippet("List<String> names = stream.toList();   // unmodifiable, null-tolerant")
                .result(service.streamToList())
                .build();
    }

    @GetMapping("/teeing")
    public Java17DemoResponse<TeeingResult> teeing(
            @RequestParam(required = false) String values) {
        return Java17DemoResponse.<TeeingResult>builder()
                .operation("Collectors.teeing(c1, c2, merger) (Java 12)")
                .description("Runs TWO downstream collectors over a SINGLE traversal of the stream, then merges their two results. It exists because a stream can only be consumed once — before Java 12 you had to collect to a list and traverse it twice, or fall back on summaryStatistics(), which only covers a fixed set of aggregates. Pass a CSV of numbers, or omit it for the default sample.")
                .codeSnippet("stream.collect(Collectors.teeing(counting(), summingDouble(x -> x), (count, sum) -> sum / count))")
                .result(service.teeing(service.parseCsv(values)))
                .build();
    }

    @GetMapping("/string-helpers")
    public Java17DemoResponse<List<StringHelperResult>> stringHelpers(
            @RequestParam(defaultValue = "dockyard") String input) {
        return Java17DemoResponse.<List<StringHelperResult>>builder()
                .operation("String additions: formatted (15), indent (12), transform (12)")
                .description("formatted() makes a template read first instead of being an argument; indent(n) adds (or, with a negative n, removes) leading spaces, normalises line endings and appends a trailing newline; transform(fn) applies an arbitrary function inline so a chain stays readable left-to-right instead of nesting inside-out.")
                .codeSnippet("\"Hello, %s!\".formatted(name);   text.indent(4);   value.transform(String::toUpperCase)")
                .result(service.stringHelpers(input))
                .build();
    }

    @GetMapping("/pitfalls")
    public Java17DemoResponse<List<String>> pitfalls() {
        return Java17DemoResponse.<List<String>>builder()
                .operation("API-addition gotchas")
                .description("The three different list contracts people conflate (Arrays.asList vs List.of vs Stream.toList), why teeing exists at all, and the surprising side effects of String.indent.")
                .codeSnippet("stream.toList().add(\"x\");  // UnsupportedOperationException — it is UNMODIFIABLE")
                .result(service.pitfalls())
                .build();
    }
}