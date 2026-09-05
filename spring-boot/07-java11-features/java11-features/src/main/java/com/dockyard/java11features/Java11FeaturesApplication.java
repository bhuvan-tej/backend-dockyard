package com.dockyard.java11features;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Java11FeaturesApplication — entry point for the Java 9–11 features tour.
 *
 * WHAT THIS APP DEMONSTRATES:
 *   The language and API additions that landed between Java 8 and Java 11
 *   (Java 11 is the next LTS release after 8, so this app picks up exactly
 *   where {@code spring-boot/06-java8-features} left off), exposed as REST
 *   endpoints so each concept can be *run* and *observed*, not just read
 *   about:
 *     - {@code var} — local-variable type inference (Java 10, JEP 286), and
 *       its Java 11 extension to lambda parameters (JEP 323), which matters
 *       because it lets you annotate an inferred lambda parameter.
 *     - New {@link String} methods — {@code isBlank}, {@code strip}/
 *       {@code stripLeading}/{@code stripTrailing} (Unicode-aware, unlike
 *       {@code trim}), {@code repeat(int)}, and {@code lines()}.
 *     - {@code Predicate.not(Predicate)}, {@link java.util.Optional#isEmpty()},
 *       and {@code Collection.toArray(IntFunction)} — small but
 *       frequently-asked additions to existing functional/collection types.
 *     - {@code Path.of(...)} and {@code Files.readString}/{@code writeString}
 *       — one-line file I/O that used to take several lines of boilerplate.
 *     - {@code java.net.http.HttpClient} — the modern, built-in HTTP client
 *       (standardized in Java 11), replacing the old
 *       {@code HttpURLConnection}, with both synchronous and asynchronous
 *       ({@code CompletableFuture}-based) request styles.
 *
 * Every endpoint returns not just a RESULT but the {@code codeSnippet} that
 * produced it and a plain-English {@code explanation} — see
 * {@code dto.Java11DemoResponse}. Read {@code LEARNING.md} for the full
 * narrative and {@code INTERVIEW_QUESTIONS.md} for the interview-prep
 * companion doc; use the endpoints to see each piece run for real.
 */
@SpringBootApplication
public class Java11FeaturesApplication {

    public static void main(String[] args) {
        SpringApplication.run(Java11FeaturesApplication.class, args);
    }

}