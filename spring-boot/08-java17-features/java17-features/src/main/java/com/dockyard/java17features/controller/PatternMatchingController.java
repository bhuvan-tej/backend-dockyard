package com.dockyard.java17features.controller;

import com.dockyard.java17features.dto.Java17DemoResponse;
import com.dockyard.java17features.dto.PatternMatchResult;
import com.dockyard.java17features.service.PatternMatchingService;
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
 * PatternMatchingController — pattern matching for {@code instanceof}
 * (JEP 394, Java 16) and switch expressions (JEP 361, Java 14). See
 * {@link PatternMatchingService} for the detailed "why".
 */
@RestController
@RequestMapping("/java17/pattern-matching")
@RequiredArgsConstructor
@Validated
public class PatternMatchingController {

    private final PatternMatchingService service;

    @GetMapping("/instanceof")
    public Java17DemoResponse<List<PatternMatchResult>> instanceOfPattern() {
        return Java17DemoResponse.<List<PatternMatchResult>>builder()
                .operation("pattern matching for instanceof (Java 16, JEP 394)")
                .description("The old test-cast-assign dance collapses into a single expression. The pattern variable can even be used in the SAME condition ('o instanceof String s && !s.isBlank()'), because the compiler knows the match already succeeded by the time it evaluates the right-hand side of &&.")
                .codeSnippet("if (o instanceof String s && !s.isBlank()) { return s.toUpperCase(); }")
                .result(service.instanceOfPattern())
                .build();
    }

    @GetMapping("/flow-scoping")
    public Java17DemoResponse<List<String>> flowScoping(
            @RequestParam(defaultValue = "dockyard") String value) {
        return Java17DemoResponse.<List<String>>builder()
                .operation("flow scoping — the rule that surprises everyone")
                .description("A pattern variable is in scope exactly where the compiler can PROVE the match succeeded. That includes the code AFTER a negated guard clause: 'if (!(o instanceof String s)) return;' leaves 's' usable on every line below. Pass a non-numeric value to see the happy path, or nothing at all to compare.")
                .codeSnippet("if (!(value instanceof String s)) { return ...; }  // 's' IS in scope from here on")
                .result(service.flowScoping(value))
                .build();
    }

    @GetMapping("/switch-expression")
    public Java17DemoResponse<PatternMatchResult> switchExpression(
            @RequestParam(defaultValue = "saturday") String day) {
        return Java17DemoResponse.<PatternMatchResult>builder()
                .operation("switch EXPRESSION with arrow labels (Java 14, JEP 361)")
                .description("A switch that produces a VALUE. Arrow labels never fall through, so there is no 'break' and no accidental fall-through bug; several constants can share one branch. The old colon-label statement form still works unchanged — this is an addition, not a replacement — but you cannot mix the two forms in one switch.")
                .codeSnippet("String kind = switch (day) { case \"SATURDAY\", \"SUNDAY\" -> \"WEEKEND\"; default -> \"UNKNOWN\"; };")
                .result(service.switchExpression(day))
                .build();
    }

    @GetMapping("/yield")
    public Java17DemoResponse<PatternMatchResult> yieldKeyword(
            @RequestParam(defaultValue = "5") @Min(-5) @Max(20) int month) {
        return Java17DemoResponse.<PatternMatchResult>builder()
                .operation("yield — returning a value from a multi-statement switch branch")
                .description("When a branch needs more than one statement it becomes a block, and a block must 'yield' the switch's value. 'return' inside a switch EXPRESSION is a compile error, because it would be ambiguous whether you meant to leave the switch or the enclosing method. Pass a month outside 1-12 to hit the default block.")
                .codeSnippet("default -> { String msg = \"...\"; log(msg); yield -1; }")
                .result(service.yieldKeyword(month))
                .build();
    }

    @GetMapping("/sealed-dispatch")
    public Java17DemoResponse<List<PatternMatchResult>> sealedDispatch() {
        return Java17DemoResponse.<List<PatternMatchResult>>builder()
                .operation("pattern matching + sealed types = exhaustive, default-free dispatch")
                .description("This is what sealing was built for: a switch over a sealed hierarchy needs no default branch, and the compiler enforces that every permitted subtype is handled. It replaces the visitor pattern and the long instanceof-else chain in one stroke. (Type patterns in switch: preview in Java 17 (JEP 406), standard in Java 21 (JEP 441) — this repo compiles with Java 21.)")
                .codeSnippet("switch (shape) { case Circle c -> ...; case Square s -> ...; case Rectangle r -> ...; }")
                .result(service.sealedDispatch())
                .build();
    }

    @GetMapping("/pitfalls")
    public Java17DemoResponse<List<String>> pitfalls() {
        return Java17DemoResponse.<List<String>>builder()
                .operation("pattern-matching and switch-expression gotchas")
                .description("Exhaustiveness rules, 'yield' vs 'return', why arrow and colon labels cannot be mixed, why a '||' condition breaks pattern-variable binding, and why a default branch over an enum quietly costs you the compile error you actually wanted.")
                .codeSnippet("if (o instanceof String s || s.isEmpty())  // ERROR — with '||' the compiler cannot prove 's' was bound")
                .result(service.pitfalls())
                .build();
    }

}