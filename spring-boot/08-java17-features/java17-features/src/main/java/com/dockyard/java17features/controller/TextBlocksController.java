package com.dockyard.java17features.controller;

import com.dockyard.java17features.dto.Java17DemoResponse;
import com.dockyard.java17features.dto.TextBlockResult;
import com.dockyard.java17features.service.TextBlocksService;
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
 * TextBlocksController — text blocks (JEP 378, Java 15). See
 * {@link TextBlocksService} for the detailed "why".
 */
@RestController
@RequestMapping("/java17/text-blocks")
@RequiredArgsConstructor
@Validated
public class TextBlocksController {

    private final TextBlocksService service;

    @GetMapping("/incidental-whitespace")
    public Java17DemoResponse<List<TextBlockResult>> incidentalWhitespace() {
        return Java17DemoResponse.<List<TextBlockResult>>builder()
                .operation("text blocks — incidental vs essential whitespace (Java 15, JEP 378)")
                .description("The compiler finds the minimum indentation across all non-blank lines AND the closing delimiter line, then strips exactly that much from every line. Indentation you add purely to keep the source readable is 'incidental' and never reaches the runtime String; anything deeper is 'essential' and is preserved. Moving the closing \"\"\" further left therefore ADDS indentation to the result.")
                .codeSnippet("""
                        String sql = \""" \
                        SELECT id, name FROM users\"""; \
                        // (the real, multi-line source lives in TextBlocksService)""")
                .result(service.incidentalWhitespace())
                .build();
    }

    @GetMapping("/escapes")
    public Java17DemoResponse<List<TextBlockResult>> escapes() {
        return Java17DemoResponse.<List<TextBlockResult>>builder()
                .operation("the two text-block escapes: \\ and \\s")
                .description("A trailing backslash SUPPRESSES that line break, letting a long single-line string be wrapped across several source lines. '\\s' is a literal space that survives the automatic trailing-whitespace stripping applied to every line. Single and double quotes need no escaping at all inside a text block — only three consecutive ones do.")
                .codeSnippet("""
                        \""" \
                        The quick brown fox \\ (line break suppressed) \
                        jumps over the lazy dog.\"""   // one single line at runtime""")
                .result(service.escapes())
                .build();
    }

    @GetMapping("/formatted")
    public Java17DemoResponse<TextBlockResult> formatted(
            @RequestParam(defaultValue = "Bhuvan") String name,
            @RequestParam(defaultValue = "3") @Min(0) @Max(999) int count) {
        return Java17DemoResponse.<TextBlockResult>builder()
                .operation("String.formatted(Object...) (Java 15) — the stand-in for string interpolation")
                .description("Java has no ${...} interpolation. 'template'.formatted(a, b) is String.format(template, a, b) turned into an instance method, which reads far better after a multi-line text block because the template stays first instead of being buried as the first argument.")
                .codeSnippet("""
                        \""" \
                        Hello, %s! You have %d unread message(s).\"""\
                        .formatted(name, count)""")
                .result(service.formatted(name, count))
                .build();
    }

    @GetMapping("/pitfalls")
    public Java17DemoResponse<List<String>> pitfalls() {
        return Java17DemoResponse.<List<String>>builder()
                .operation("text-block gotchas")
                .description("Content must begin on the line AFTER the opening delimiter; trailing whitespace is stripped silently on every line; line endings are normalised to \\n; the position of the closing delimiter changes both indentation and whether a trailing newline is added.")
                .codeSnippet("String s = \"\"\"abc\"\"\";  // ERROR — content must start on the next line")
                .result(service.pitfalls())
                .build();
    }

}