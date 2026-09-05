package com.dockyard.java11features.service;

import com.dockyard.java11features.dto.StripVariantsResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * StringMethodsService — small but frequently-tested {@link String} additions
 * from Java 11.
 *
 * <ul>
 *   <li>{@code isBlank()} — true if the string is empty OR contains only
 *       whitespace (per {@link Character#isWhitespace(char)}) — stricter
 *       than {@code isEmpty()}, which only checks length == 0.</li>
 *   <li>{@code strip()}/{@code stripLeading()}/{@code stripTrailing()} —
 *       Unicode-aware whitespace removal, unlike the legacy {@code trim()}
 *       which only strips characters {@code <= U+0020} (so it MISSES some
 *       Unicode whitespace, and WRONGLY strips some C0 control characters
 *       that aren't whitespace at all).</li>
 *   <li>{@code repeat(int)}) — concatenates the string with itself N times;
 *       replaces the old {@code String.join("", Collections.nCopies(n, s))}
 *       or a manual {@code StringBuilder} loop.</li>
 *   <li>{@code lines()} — splits on line terminators ({@code \n},
 *       {@code \r\n}, {@code \r}) and returns a {@code Stream<String>},
 *       without the caller needing a regex.</li>
 * </ul>
 */
@Service
public class StringMethodsService {

    /** isBlank() vs isEmpty() — isBlank additionally treats whitespace-only strings as blank. */
    public String isBlankDemo(String value) {
        boolean isBlank = value.isBlank();
        boolean isEmpty = value.isEmpty();
        return String.format("\"%s\".isBlank() = %s, \"%s\".isEmpty() = %s%s",
                value, isBlank, value, isEmpty,
                (isBlank && !isEmpty) ? " — blank because it's ALL whitespace, but NOT empty (length > 0)" : "");
    }

    /**
     * strip()/stripLeading()/stripTrailing() are Unicode-aware; trim() is not.
     * U+2003 (EM SPACE) passes {@link Character#isWhitespace(char)} but is
     * ABOVE U+0020, so legacy trim() (which only strips characters
     * {@code <= U+0020}) leaves it behind, while strip() correctly removes
     * it — this is the clearest strip-vs-trim example. (Careful: some
     * Unicode "space" characters — U+00A0 NBSP, U+2007 FIGURE SPACE, U+202F
     * NNBSP — are deliberately EXCLUDED from isWhitespace() and are left
     * behind by strip() too, which is a common gotcha in its own right.)
     */
    public StripVariantsResult stripVariants() {
        String emSpace = "\u2003"; // real Unicode whitespace (Character.isWhitespace == true), but > U+0020 so trim() ignores it
        String original = emSpace + "hello world" + emSpace;

        String trimResult = original.trim();
        String stripResult = original.strip();
        String stripLeadingResult = original.stripLeading();
        String stripTrailingResult = original.stripTrailing();

        return StripVariantsResult.builder()
                .originalWithMarkers("[EM-SPACE]hello world[EM-SPACE]")
                .trimResult(describe(trimResult))
                .stripResult(describe(stripResult))
                .stripLeadingResult(describe(stripLeadingResult))
                .stripTrailingResult(describe(stripTrailingResult))
                .trimAndStripAgree(trimResult.equals(stripResult))
                .build();
    }

    private String describe(String s) {
        return s.equals("hello world") ? "\"hello world\" (whitespace fully removed)" : "[" + s.length() + " chars, still contains U+2003] \"" + s + "\"";
    }

    /** repeat(int) — string concatenation without a manual loop or StringBuilder. */
    public String repeat(String value, int count) {
        return value.repeat(count); // equivalent, pre-Java-11: String.join("", Collections.nCopies(count, value))
    }

    /** lines() — splits on \n, \r\n, or \r, returning a Stream<String>, collected here into a List for JSON. */
    public List<String> lines(String text) {
        List<String> result = new ArrayList<>();
        text.lines().forEach(result::add);
        return result;
    }

}