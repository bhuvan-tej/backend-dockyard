package com.dockyard.qrgenerator.config;

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
 *
 * Swagger lets you generate and download real QR codes straight from the
 * browser — no Postman or curl needed to try the API.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("QR Generator API")
                        .description("""
                                Generate, decode and analyse QR codes.

                                Features:
                                  PNG or Base64 data-URI output with colour, size and margin control
                                  Error-correction levels (L/M/Q/H)
                                  WiFi and vCard helper endpoints (real-world use cases)
                                  Decode a QR code back to text from an uploaded image
                                  Searchable generation history and usage analytics
                                """)
                        .version("1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Local development server")
                ));
    }
}