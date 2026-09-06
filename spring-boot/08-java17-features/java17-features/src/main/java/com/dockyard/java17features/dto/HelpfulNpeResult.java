package com.dockyard.java17features.dto;

import lombok.Builder;

/**
 * HelpfulNpeResult — the actual message text of a deliberately-triggered
 * {@link NullPointerException}. Since JEP 358 (Java 14, on by default from
 * Java 15) the JVM describes exactly WHICH expression was null, instead of
 * the old bare "java.lang.NullPointerException" with only a line number.
 */
@Builder
public record HelpfulNpeResult(
        String expression,
        String message,
        String preJava14Message,
        String note
) { }

