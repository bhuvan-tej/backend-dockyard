package com.dockyard.springai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI metadata. Browse the API at /api/swagger-ui.html once running. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI springAiBasicsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Spring AI Basics")
                .version("1.0.0")
                .description("The five core ways to talk to an LLM with Spring AI: "
                        + "ask, persona (system prompt), template, structured output, and streaming."));
    }
}