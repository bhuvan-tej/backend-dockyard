package com.dockyard.otpauth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
 * The server URL includes the /api context-path so Swagger's "Execute" button
 * targets the real endpoints. A "bearerAuth" scheme is declared so you can paste
 * an access token via the Authorize button and call the protected /auth/me route.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OTP + JWT Auth API")
                        .description("""
                                Passwordless authentication with strict validity and expiry.

                                Flow:
                                  1. POST /otp/request   → get a short-lived OTP (dev mode returns it)
                                  2. POST /otp/verify     → exchange a valid OTP for JWT tokens
                                  3. GET  /auth/me        → call a protected endpoint with the access token
                                  4. POST /auth/refresh   → swap a refresh token for a fresh access token

                                Everything expires: the OTP, the access token and the refresh token,
                                each on its own clock and enforced server-side.
                                """)
                        .version("1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Local development server")))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the accessToken returned by /otp/verify")));
    }
}