package com.dockyard.virtualthreads.config;

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
                        .title("Virtual Threads Demo")
                        .description("""
                                Java 21 virtual threads vs platform threads, side by side.

                                Try:
                                  1. GET /demo/run?executor=platform&tasks=100&delayMs=200 → slow, queues up
                                  2. GET /demo/run?executor=virtual&tasks=100&delayMs=200  → fast, no queueing
                                  3. GET /demo/compare?tasks=100&delayMs=200               → both, one response
                                  4. GET /demo/request-thread                              → which thread served THIS request?

                                Each task just sleeps for delayMs — simulating a slow downstream
                                HTTP call or DB query. Only the executor running them changes.
                                """)
                        .version("1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Local development server")));
    }
}