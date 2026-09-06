package com.dockyard.java17features.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenApiConfig — configures the Swagger UI documentation.
 *
 * Swagger UI:  http://localhost:8080/api/swagger-ui.html
 * Raw spec:    http://localhost:8080/api/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Java 12-17 Features — A Guided Tour")
                        .description("""
                                The language and API additions between Java 11 and Java 17 (the next LTS),
                                demonstrated end to end with runnable REST endpoints.

                                Read in this order:
                                  1. /java17/records/*          → records, compact constructors, what they can't do
                                  2. /java17/sealed/*           → sealed classes/interfaces, permits, closed hierarchies
                                  3. /java17/pattern-matching/* → instanceof patterns, switch expressions, yield
                                  4. /java17/text-blocks/*      → multi-line literals, incidental whitespace, \\ and \\s
                                  5. /java17/api/*              → Stream.toList, Collectors.teeing, String helpers
                                  6. /java17/jdk/*              → helpful NPEs, RandomGenerator, compact number format

                                Every endpoint returns not just a result, but the exact code snippet that
                                produced it and a plain-English explanation — see LEARNING.md for the full
                                narrative and INTERVIEW_QUESTIONS.md for interview prep.
                                """)
                        .version("1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Local development server")));
    }

}