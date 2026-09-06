package com.dockyard.java17features;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Java17FeaturesApplication — entry point for the Java 12–17 features tour.
 *
 * WHAT THIS APP DEMONSTRATES:
 *   The language and API additions that landed between Java 11 and Java 17
 *   (Java 17 is the next LTS release after 11, so this app picks up exactly
 *   where {@code spring-boot/07-java11-features} left off), exposed as REST
 *   endpoints so each concept can be *run* and *observed*, not just read
 *   about:
 *     - <b>Records</b> (JEP 395, Java 16) — transparent carriers for
 *       immutable data. The compiler generates the constructor, accessors,
 *       {@code equals}, {@code hashCode} and {@code toString} for you; the
 *       compact constructor is where validation/normalisation lives.
 *     - <b>Sealed classes and interfaces</b> (JEP 409, Java 17) — a type
 *       that controls exactly which types may extend/implement it, giving
 *       the compiler a CLOSED set it can reason about exhaustively.
 *     - <b>Pattern matching for {@code instanceof}</b> (JEP 394, Java 16)
 *       and <b>switch expressions</b> (JEP 361, Java 14) — arrow labels,
 *       {@code yield}, exhaustiveness, and no more accidental fall-through.
 *     - <b>Text blocks</b> (JEP 378, Java 15) — multi-line string literals
 *       with incidental-whitespace stripping and the {@code \} and
 *       {@code \s} escapes.
 *     - <b>Stream/Collector/String API additions</b> —
 *       {@code Stream.toList()} (16), {@code Collectors.teeing()} (12),
 *       {@code String.formatted}/{@code indent}/{@code transform} (12/15).
 *     - <b>Runtime/JDK additions</b> — helpful {@code NullPointerException}
 *       messages (JEP 358, Java 14), the pluggable {@code RandomGenerator}
 *       API (JEP 356, Java 17), and compact number formatting (Java 12).
 *
 * Every endpoint returns not just a RESULT but the {@code codeSnippet} that
 * produced it and a plain-English {@code description} — see
 * {@code dto.Java17DemoResponse}. Read {@code LEARNING.md} for the full
 * narrative and {@code INTERVIEW_QUESTIONS.md} for the interview-prep
 * companion doc; use the endpoints to see each piece run for real.
 */
@SpringBootApplication
public class Java17FeaturesApplication {

    public static void main(String[] args) {
        SpringApplication.run(Java17FeaturesApplication.class, args);
    }

}