package com.dockyard.otpauth.config;

import com.dockyard.otpauth.security.JwtAuthenticationFilter;
import com.dockyard.otpauth.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig — wires the stateless JWT security model.
 *
 * DESIGN:
 *   - No sessions, no CSRF: this is a token API, every request carries its own
 *     proof (the Bearer access token), so there is no session to protect.
 *   - PUBLIC endpoints: OTP request/verify and token refresh (you cannot have a
 *     token yet when you are trying to get one), plus Swagger, H2 and health.
 *   - EVERYTHING ELSE requires a valid access token (e.g. /auth/me).
 *   - Our JwtAuthenticationFilter runs before the username/password filter and
 *     populates the security context from the token.
 *
 * NOTE: matchers are written WITHOUT the /api context-path — Spring Security
 * matches the path after the context-path is stripped.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Token API: disable CSRF and never create an HTTP session.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Allow the H2 console to render inside a frame (dev only).
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

                .authorizeHttpRequests(auth -> auth
                        // --- public: getting or refreshing a token ---
                        .requestMatchers("/otp/**", "/auth/refresh").permitAll()
                        // --- public: docs, health, dev console ---
                        .requestMatchers(
                                "/swagger-ui/**", "/swagger-ui.html", "/api-docs/**",
                                "/actuator/health", "/actuator/info",
                                "/h2-console/**").permitAll()
                        // --- everything else needs a valid access token ---
                        .anyRequest().authenticated())

                // Return our JSON ErrorResponse on 401 instead of an HTML page.
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))

                // Plug the JWT filter into the chain.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt encoder — used to HASH the OTP before storing it. BCrypt is
     * deliberately slow and salted, which is exactly what you want for a secret
     * that guards access, even a short-lived one.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}