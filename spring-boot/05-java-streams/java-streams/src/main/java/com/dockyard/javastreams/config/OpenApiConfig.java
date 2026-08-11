package com.dockyard.javastreams.config;

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
                        .title("Java Streams — A Guided Tour")
                        .description("""
                                Every corner of java.util.stream AS IT EXISTED IN JAVA 8, demonstrated
                                against one shared 20-employee dataset.

                                Read in this order:
                                  1. /streams/creation/*      → how to OBTAIN a stream
                                  2. /streams/intermediate/*  → lazy operations that transform a stream into another stream
                                  3. /streams/terminal/*      → the operations that actually RUN the pipeline
                                  4. /streams/collectors/*    → turning a stream back into a List/Set/Map/report
                                  5. /streams/parallel/*      → stream() vs parallelStream(), and the pitfall to avoid

                                Every endpoint returns not just a result, but the exact code snippet that
                                produced it and a plain-English explanation — see LEARNING.md for the full
                                narrative.
                                """)
                        .version("1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Local development server")));
    }
}