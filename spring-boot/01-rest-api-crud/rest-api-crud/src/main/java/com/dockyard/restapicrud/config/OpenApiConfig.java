package com.dockyard.restapicrud.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenApiConfig — configures the Swagger UI documentation.
 *
 * Access Swagger UI at: http://localhost:8080/swagger-ui.html
 * Access raw API docs at: http://localhost:8080/api/api-docs
 *
 * WHY SWAGGER UI?
 *   Automatically documents every endpoint from annotations
 *   Test the API directly from the browser without Postman
 *   Frontend and client developers explore the API without reading code
 *   Always in sync with the actual code — never goes stale
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Products REST API")
                        .description("""
                                REST API demonstrating:
                                  Bean Validation with custom error responses
                                  Pagination and sorting
                                  Global exception handling
                                  Full CRUD with correct HTTP status codes
                                  Search and category filtering
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("backend-dockyard")
                                .url("https://github.com/YOUR_USERNAME/backend-dockyard"))
                        .license(new License()
                                .name("MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server")
                ));
    }
}