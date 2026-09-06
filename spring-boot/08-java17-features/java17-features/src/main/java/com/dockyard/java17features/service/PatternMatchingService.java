package com.dockyard.java17features.service;

import com.dockyard.java17features.domain.Point;
import com.dockyard.java17features.domain.Shape;
import com.dockyard.java17features.dto.PatternMatchResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * PatternMatchingService — two closely-related features that together kill a
 * lot of ceremonial Java:
 *
 * <h2>1. Pattern matching for {@code instanceof} (JEP 394, Java 16)</h2>
 * The old three-step dance — test, cast, assign — collapses into one:
 * <pre>
 *   if (o instanceof String) { String s = (String) o; ... }   // before
 *   if (o instanceof String s) { ... }                        // after
 * </pre>
 * {@code s} is a PATTERN VARIABLE. Its scope is governed by "flow scoping":
 * it exists exactly where the compiler can PROVE the match succeeded — which
 * is why {@code if (!(o instanceof String s)) return; /* s is in scope here! *&#47;}
 * works. That surprises people, and it's deliberate.
 *
 * <h2>2. Switch expressions (JEP 361, Java 14)</h2>
 * A {@code switch} that PRODUCES A VALUE, with arrow labels that don't fall
 * through:
 * <ul>
 *   <li>{@code case A, B -> expr;} — no {@code break}, no accidental
 *       fall-through, multiple labels per branch.</li>
 *   <li>{@code yield} returns a value from a multi-statement
 *       {@code { ... }} branch. ({@code return} inside a switch EXPRESSION is
 *       illegal — it would be ambiguous whether you meant the switch or the
 *       enclosing method.)</li>
 *   <li>A switch EXPRESSION must be exhaustive; over an enum that means
 *       covering every constant (or adding {@code default}).</li>
 * </ul>
 * The old colon-label statement form still works, unchanged — arrow labels
 * are an addition, not a replacement.
 */
@Service
public class PatternMatchingService {

    /** instanceof pattern matching — test, cast and bind in one step. */
    public List<PatternMatchResult> instanceOfPattern() {
        List<Object> inputs = List.of("hello world", 42, 3.5, List.of(1, 2, 3), new Point(3, 4));
        return inputs.stream().map(this::describe).toList();
    }

    private PatternMatchResult describe(Object o) {
        String matched;
        String output;

        // Each branch tests, casts AND binds in a single expression. The pattern
        // variable is only in scope where the compiler can prove the test passed.
        if (o instanceof String s && !s.isBlank()) {           // extra condition on the bound variable
            matched = "String s && !s.isBlank()";
            output = s.toUpperCase();
        } else if (o instanceof Integer i) {
            matched = "Integer i";
            output = "squared = " + (i * i);
        } else if (o instanceof Double d) {
            matched = "Double d";
            output = "rounded = " + Math.round(d);
        } else if (o instanceof List<?> list) {
            matched = "List<?> list";
            output = "size = " + list.size();
        } else if (o instanceof Point p) {                     // records work naturally with patterns
            matched = "Point p";
            output = "x+y = " + (p.x() + p.y());
        } else {
            matched = "(no pattern matched)";
            output = String.valueOf(o);
        }

        return PatternMatchResult.builder()
                .input(String.valueOf(o))
                .runtimeType(o.getClass().getSimpleName())
                .matchedPattern(matched)
                .output(output)
                .build();
    }

    /**
     * FLOW SCOPING — the rule that surprises people. The pattern variable is
     * in scope wherever the compiler can prove the match succeeded, which
     * includes the code AFTER an early return in the negated form.
     *
     * <p>A purely numeric input is boxed to an {@link Integer} first, so the
     * "not a String" branch is reachable from the endpoint too.</p>
     */
    public List<String> flowScoping(String raw) {
        Object value = raw != null && raw.matches("-?\\d+") ? Integer.valueOf(raw) : raw;

        // Deliberately written in the negated "guard clause" style.
        if (!(value instanceof String s)) {
            return List.of(
                    "Input was a " + value.getClass().getSimpleName() + ", not a String — 's' is NOT in scope on this path.",
                    "Flow scoping: the compiler only admits 's' where the match is PROVEN to have succeeded.",
                    "Pass a non-numeric value to see the other branch.");
        }
        // 's' IS in scope here — because the only way to reach this line is if the
        // instanceof succeeded. This is the single most surprising (and most useful)
        // consequence of flow scoping.
        return List.of(
                "Reached the happy path, so 's' is in scope even though it was bound inside a NEGATED instanceof.",
                "s.length() = " + s.length(),
                "s.toUpperCase() = " + s.toUpperCase());
    }

    /** Switch EXPRESSION with arrow labels — produces a value, no break, no fall-through. */
    public PatternMatchResult switchExpression(String day) {
        String normalized = day == null ? "" : day.strip().toUpperCase();

        // Arrow labels: multiple constants per branch, no 'break', no fall-through,
        // and the whole switch is an EXPRESSION assigned to a variable.
        String kind = switch (normalized) {
            case "SATURDAY", "SUNDAY" -> "WEEKEND";
            case "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY" -> "WEEKDAY";
            default -> "UNKNOWN";
        };

        return PatternMatchResult.builder()
                .input(day)
                .runtimeType("String")
                .matchedPattern("case \"SATURDAY\", \"SUNDAY\" -> ... (arrow label, multiple constants)")
                .output(kind)
                .build();
    }

    /** {@code yield} — returning a value from a multi-statement switch branch. */
    public PatternMatchResult yieldKeyword(int month) {
        int quarter = switch (month) {
            case 1, 2, 3 -> 1;
            case 4, 5, 6 -> 2;
            case 7, 8, 9 -> 3;
            case 10, 11, 12 -> 4;
            default -> {
                // A block branch needs 'yield' to produce the switch's value.
                // 'return' here would be a compile error — it's ambiguous whether
                // you meant to leave the switch or the enclosing method.
                String message = "month must be 1-12, was " + month;
                System.out.println("[yieldKeyword] " + message);
                yield -1;
            }
        };

        return PatternMatchResult.builder()
                .input(String.valueOf(month))
                .runtimeType("int")
                .matchedPattern("default -> { ...; yield -1; }")
                .output(quarter == -1 ? "invalid month" : "Q" + quarter)
                .build();
    }

    /** Pattern matching + a sealed hierarchy = exhaustive, default-free dispatch. */
    public List<PatternMatchResult> sealedDispatch() {
        List<Shape> shapes = List.of(
                new Shape.Circle(2),
                new Shape.Square(3),
                new Shape.Rectangle(2, 5));

        return shapes.stream()
                .map(shape -> {
                    // Type patterns in a switch: preview in Java 17 (JEP 406), standard
                    // in Java 21 (JEP 441). No 'default' is needed because Shape is sealed.
                    String description = switch (shape) {
                        case Shape.Circle c -> "circle with radius " + c.radius();
                        case Shape.Square s -> "square with side " + s.side();
                        case Shape.Rectangle r -> "rectangle " + r.width() + " x " + r.height();
                    };
                    return PatternMatchResult.builder()
                            .input(shape.toString())
                            .runtimeType(shape.getClass().getSimpleName())
                            .matchedPattern("case " + shape.getClass().getSimpleName() + " x -> ...")
                            .output(description + ", area " + String.format("%.3f", shape.area()))
                            .build();
                })
                .toList();
    }

    /** The gotchas that get asked about. */
    public List<String> pitfalls() {
        return List.of(
                "A switch EXPRESSION must be exhaustive — over an enum, cover every constant or add default; over a sealed type, cover every permitted subtype",
                "'return' inside a switch EXPRESSION is a COMPILE ERROR — use 'yield' to produce the branch's value",
                "'yield' is only for switch EXPRESSIONS; a switch STATEMENT with arrow labels needs no yield at all",
                "Arrow labels never fall through — the old 'case X:' colon form still does, and still needs 'break'",
                "You cannot MIX arrow labels and colon labels in the same switch",
                "A switch expression over an enum with a 'default' branch loses the compile error you WANT when a new constant is added — prefer exhaustive cases without default",
                "instanceof pattern variables are FLOW SCOPED — 'if (!(o instanceof String s)) return;' leaves 's' in scope afterwards, by design",
                "if (o instanceof String s || s.isEmpty())  // COMPILE ERROR — with '||' the compiler cannot prove 's' was bound",
                "switch on a null selector throws NullPointerException unless a 'case null' label is present (case null is Java 21, not 17)"
        );
    }

}