package com.dockyard.java11features.controller;

import com.dockyard.java11features.dto.Java11DemoResponse;
import com.dockyard.java11features.dto.StripVariantsResult;
import com.dockyard.java11features.service.StringMethodsService;
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
 * StringMethodsController — String.isBlank/strip/repeat/lines.
 * See {@link StringMethodsService} for the detailed "why".
 */
@RestController
@RequestMapping("/java11/string")
@RequiredArgsConstructor
@Validated
public class StringMethodsController {

    private final StringMethodsService service;

    @GetMapping("/is-blank")
    public Java11DemoResponse<String> isBlank(
            @RequestParam(defaultValue = "   ") String value) {
        return Java11DemoResponse.<String>builder()
                .operation("String.isBlank() vs isEmpty()")
                .description("isEmpty() only checks length == 0. isBlank() additionally treats a string made ENTIRELY of whitespace as blank — 'if (input.isBlank())' replaces the common but subtly wrong 'if (input.trim().isEmpty())'.")
                .codeSnippet("\"   \".isBlank() == true;  \"   \".isEmpty() == false;")
                .result(service.isBlankDemo(value))
                .build();
    }

    @GetMapping("/strip-variants")
    public Java11DemoResponse<StripVariantsResult> stripVariants() {
        return Java11DemoResponse.<StripVariantsResult>builder()
                .operation("strip()/stripLeading()/stripTrailing() vs the legacy trim()")
                .description("strip() and friends are Unicode-aware (Character.isWhitespace) — the legacy trim() only strips characters <= U+0020, so it MISSES real Unicode whitespace like U+2003 (EM SPACE), demonstrated here.")
                .codeSnippet("String s = \"\\u2003hello world\\u2003\"; s.trim(); // leaves U+2003 behind\ns.strip();  // correctly removes it")
                .result(service.stripVariants())
                .build();
    }

    @GetMapping("/repeat")
    public Java11DemoResponse<String> repeat(
            @RequestParam(defaultValue = "ab") String value,
            @RequestParam(defaultValue = "3") @Min(0) @Max(50) int count) {
        return Java11DemoResponse.<String>builder()
                .operation("String.repeat(int)")
                .description("Concatenates the string with itself 'count' times — replaces the older String.join(\"\", Collections.nCopies(count, value)) or a manual StringBuilder loop.")
                .codeSnippet("\"ab\".repeat(3); // \"ababab\"")
                .result(service.repeat(value, count))
                .build();
    }

    @GetMapping("/lines")
    public Java11DemoResponse<List<String>> lines(
            @RequestParam(defaultValue = "line one\nline two\r\nline three") String text) {
        return Java11DemoResponse.<List<String>>builder()
                .operation("String.lines()")
                .description("Splits on any line terminator (\\n, \\r\\n, or \\r) and returns a Stream<String> — no regex required, and it correctly handles mixed line-ending styles in the same string.")
                .codeSnippet("text.lines().forEach(System.out::println);")
                .result(service.lines(text))
                .build();
    }

}