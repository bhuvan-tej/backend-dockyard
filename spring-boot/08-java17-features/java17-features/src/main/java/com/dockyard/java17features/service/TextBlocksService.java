package com.dockyard.java17features.service;

import com.dockyard.java17features.dto.TextBlockResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * TextBlocksService — text blocks (JEP 378, standard in <b>Java 15</b>) are
 * multi-line string literals delimited by {@code """}.
 *
 * <h2>What the compiler does for you</h2>
 * <ol>
 *   <li><b>Incidental whitespace is stripped.</b> The compiler finds the
 *       minimum indentation across all non-blank lines <i>and</i> the closing
 *       delimiter line, then removes exactly that much from every line. So
 *       the indentation you add to keep the source readable does NOT end up
 *       in the runtime String. Moving the closing {@code """} further left
 *       therefore ADDS indentation to the result — the delimiter line
 *       participates in the calculation.</li>
 *   <li><b>Trailing whitespace on each line is stripped</b> — always, and
 *       invisibly. Use the {@code \s} escape to protect a space you actually
 *       want.</li>
 *   <li><b>Line terminators are normalised to {@code \n}</b>, so the same
 *       source produces the same String on Windows and Unix.</li>
 * </ol>
 *
 * <h2>The two new escapes</h2>
 * <ul>
 *   <li>{@code \} at end of line — SUPPRESSES that line break, letting you
 *       wrap a long single-line string across several source lines.</li>
 *   <li>{@code \s} — a literal space that survives trailing-whitespace
 *       stripping.</li>
 * </ul>
 *
 * <h2>What it is NOT</h2>
 * A text block is still just a {@code String} — same type, same interning,
 * no interpolation. There is no {@code ${...}} substitution in Java; use
 * {@link String#formatted(Object...)} (Java 15) or {@code String.format}.
 */
@Service
public class TextBlocksService {

    /** Incidental vs essential whitespace — the core rule. */
    @SuppressWarnings("TextBlockMigration")   // the concatenation below is the deliberate "before" example
    public List<TextBlockResult> incidentalWhitespace() {
        // Every line below is indented in the SOURCE for readability. The compiler
        // computes the minimum indentation across non-blank lines AND the closing
        // delimiter line, and removes exactly that much. The result has no leading
        // spaces on "SELECT" at all.
        String sql = """
                SELECT id, name, email
                FROM users
                WHERE active = true
                ORDER BY name""";

        // Here the inner lines are indented FURTHER than the closing delimiter, so
        // that extra indentation is ESSENTIAL and is preserved in the result.
        String nested = """
                {
                    "name": "dockyard",
                    "tags": ["java", "17"]
                }
                """;

        // Kept deliberately as pre-Java-15 concatenation — it is the "before" half of
        // the comparison, and the endpoint proves it is .equals() to the text block above.
        String equivalentPreJava15 =
                "SELECT id, name, email\n" +
                "FROM users\n" +
                "WHERE active = true\n" +
                "ORDER BY name";

        return List.of(
                describe("sql (closing \"\"\" on the last content line)", sql,
                        "No trailing newline, because the closing delimiter sits at the END of the last content line."),
                describe("nested (deeper indentation is ESSENTIAL and preserved)", nested,
                        "The 4-space indent inside the braces survives — it's deeper than the minimum, so it is not incidental."),
                describe("identical to the pre-Java-15 concatenation", equivalentPreJava15,
                        "Same String, same type. sql.equals(this) == " + sql.equals(equivalentPreJava15)));
    }

    /** The {@code \} (join lines) and {@code \s} (keep a space) escapes. */
    public List<TextBlockResult> escapes() {
        // Trailing '\' suppresses the line break — one logical line, wrapped in source.
        String joined = """
                The quick brown fox \
                jumps over \
                the lazy dog.""";

        // '\s' is a literal space that survives trailing-whitespace stripping.
        // Without it, the spaces after "red" and "green" would be silently removed.
        String padded = """
                red  \s
                green\s
                blue""";

        // A text block can contain " and "" freely; only three in a row need escaping.
        String quotes = """
                He said "hello" and then ""double"" quoted it.
                Three in a row must be escaped: \""" like that.""";

        return List.of(
                describe("\\ at end of line — suppresses the line break", joined,
                        "One line at runtime, three in the source. lineCount proves it."),
                describe("\\s — a space that survives trailing-whitespace stripping", padded,
                        "Trailing whitespace is ALWAYS stripped from each line; \\s is how you keep one on purpose."),
                describe("quotes inside a text block", quotes,
                        "Single and double quotes need no escaping at all — only three consecutive \" do."));
    }

    /** {@link String#formatted(Object...)} (Java 15) — the substitute for string interpolation. */
    public TextBlockResult formatted(String name, int count) {
        // Java has NO string interpolation. formatted(...) is an instance-method
        // version of String.format(...) that reads far better on a text block,
        // because the arguments follow the block instead of preceding it.
        String message = """
                Hello, %s!
                You have %d unread message(s).
                Generated by java17-features.
                """.formatted(name, count);

        return describe("text block + String.formatted(...)", message,
                "\"...\".formatted(a, b) is equivalent to String.format(\"...\", a, b) — but the template stays first, which reads much better for a multi-line block.");
    }

    /** Things people get wrong about text blocks. */
    public List<String> pitfalls() {
        return List.of(
                "String s = \"\"\"abc\"\"\";              // COMPILE ERROR — content must start on the line AFTER the opening \"\"\"",
                "Trailing whitespace on every line is stripped SILENTLY — use \\s to keep a space you actually need",
                "Moving the closing \"\"\" further LEFT adds indentation to the result — the delimiter line takes part in the minimum-indent calculation",
                "Closing \"\"\" on its own line adds a trailing \\n; putting it at the end of the last content line does not",
                "Line endings are normalised to \\n regardless of the platform the file was written on",
                "A text block is just a String — no interpolation. Use .formatted(...) or String.format(...)",
                "Only THREE consecutive double quotes need escaping; one or two are fine as-is",
                "stripIndent() / translateEscapes() (both Java 15) let you apply the same processing to an ordinary String at runtime"
        );
    }

    private TextBlockResult describe(String label, String value, String note) {
        return TextBlockResult.builder()
                .label(label)
                .value(value)
                .length(value.length())
                .lineCount(value.lines().count())
                .note(note)
                .build();
    }

}