package com.dockyard.java8features.config;

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
                        .title("Core Java 8 Features — A Guided Tour")
                        .description("""
                                The Java 8 language features every interview expects you to know,
                                demonstrated end to end with runnable REST endpoints.

                                Read in this order:
                                  1. /java8/lambda/*               → lambda syntax forms, variable capture
                                  2. /java8/functional-interfaces/* → Predicate, Function, Supplier, Consumer, BiFunction, custom
                                  3. /java8/method-references/*     → static, bound, unbound, constructor references
                                  4. /java8/interface-methods/*     → default & static interface methods, the diamond problem
                                  5. /java8/optional/*              → Optional as an explicit alternative to null
                                  6. /java8/datetime/*              → java.time: LocalDate/Time, Period, Duration, formatting

                                The Stream API has its own dedicated app — see spring-boot/05-java-streams —
                                so it isn't duplicated here.

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