package com.dockyard.javastreams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * JavaStreamsApplication — entry point for the Java 8 Stream API tour.
 *
 * WHAT THIS APP DEMONSTRATES:
 *   Every corner of {@code java.util.stream} AS IT EXISTED IN JAVA 8 — no
 *   {@code Stream.toList()} (that's Java 16), no {@code takeWhile}/{@code
 *   dropWhile} (Java 9), no {@code Collectors.teeing} (Java 12). The goal is
 *   a clean, unambiguous picture of the ORIGINAL Stream API: how a pipeline
 *   is built (source → intermediate ops → terminal op), which operations are
 *   lazy vs eager, which are short-circuiting, and how {@code Collectors}
 *   turns a stream back into a useful shape (list, map, grouped report...).
 *
 * Every endpoint returns not just a RESULT but the {@code codeSnippet} that
 * produced it and a plain-English {@code explanation} — see
 * {@code dto.StreamDemoResponse}. Read {@code LEARNING.md} for the full
 * narrative; use the endpoints to see each piece run against real data.
 */
@SpringBootApplication
public class JavaStreamsApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaStreamsApplication.class, args);
    }

}