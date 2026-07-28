package com.dockyard.semanticsearch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenApiConfig — configures the Swagger UI documentation.
 *
 * <p>Swagger UI:  http://localhost:8080/api/swagger-ui.html
 * <br>Raw spec:   http://localhost:8080/api/api-docs
 *
 * <p>The server URL includes the /api context-path so Swagger's "Execute" button
 * targets the real endpoints.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Semantic Search API")
                        .description("""
                                Search text by MEANING using embeddings — no LLM involved.

                                Flow:
                                  1. POST /search/index    → embed & store some texts
                                  2. GET  /search          → find the nearest texts to a query
                                  3. GET  /search/embed    → see the raw vector a text becomes
                                  4. GET  /search/compare  → cosine similarity of two texts
                                  5. GET  /search/stats    → how many texts are indexed

                                This is the retrieval half of RAG, isolated so the core idea
                                (meaning → vectors → nearest-neighbour search) is easy to grasp.
                                """)
                        .version("1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Local development server")));
    }

}