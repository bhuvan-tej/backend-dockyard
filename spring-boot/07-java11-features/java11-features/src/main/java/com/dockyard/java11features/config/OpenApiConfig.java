package com.dockyard.java11features.config;

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
                        .title("Java 9-11 Features — A Guided Tour")
                        .description("""
                                The language and API additions between Java 8 and Java 11 (the next LTS),
                                demonstrated end to end with runnable REST endpoints.

                                Read in this order:
                                  1. /java11/var/*         → local-variable type inference, var in lambda params
                                  2. /java11/string/*      → isBlank, strip/stripLeading/stripTrailing, repeat, lines
                                  3. /java11/functional/*  → Predicate.not, Optional.isEmpty, Collection.toArray(IntFunction)
                                  4. /java11/nio/*         → Path.of, Files.readString/writeString
                                  5. /java11/httpclient/*  → java.net.http.HttpClient — sync and async requests

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