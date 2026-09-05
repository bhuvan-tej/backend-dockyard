package com.dockyard.java8features;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Java8FeaturesApplication — entry point for the core Java 8 features tour.
 *
 * WHAT THIS APP DEMONSTRATES:
 *   The language-level features that shipped in Java 8 and reshaped how
 *   idiomatic Java is written, exposed as REST endpoints so each concept can
 *   be *run* and *observed*, not just read about:
 *     - Lambda expressions — syntax forms, variable capture, and how they
 *       differ from anonymous classes.
 *     - Functional interfaces — the {@code java.util.function} family
 *       ({@code Predicate}, {@code Function}, {@code Supplier}, {@code Consumer},
 *       {@code BiFunction}) plus writing your own with {@code @FunctionalInterface}.
 *     - Method references — the four flavors: static, bound instance,
 *       unbound instance, and constructor references.
 *     - Interface evolution — {@code default} and {@code static} methods on
 *       interfaces, and how the "diamond problem" is resolved with
 *       {@code InterfaceName.super.method()}.
 *     - {@link java.util.Optional} — an explicit alternative to returning
 *       {@code null}, including the classic {@code orElse} vs {@code orElseGet}
 *       eager-vs-lazy gotcha.
 *     - The new {@code java.time} API — {@code LocalDate}/{@code LocalTime}/
 *       {@code LocalDateTime}, {@code Period} vs {@code Duration}, and why it
 *       replaced the mutable, not-thread-safe {@code java.util.Date}/{@code Calendar}.
 *
 * The Stream API (Java 8's other headline feature) has its own dedicated,
 * deep-dive app — see {@code spring-boot/05-java-streams} — so it isn't
 * duplicated here.
 *
 * Every endpoint returns not just a RESULT but the {@code codeSnippet} that
 * produced it and a plain-English {@code explanation} — see
 * {@code dto.Java8DemoResponse}. Read {@code LEARNING.md} for the full
 * narrative and {@code INTERVIEW_QUESTIONS.md} for the interview-prep
 * companion doc; use the endpoints to see each piece run for real.
 */
@SpringBootApplication
public class Java8FeaturesApplication {

    public static void main(String[] args) {
        SpringApplication.run(Java8FeaturesApplication.class, args);
    }

}