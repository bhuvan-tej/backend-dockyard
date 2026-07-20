package com.dockyard.otpauth.security;

import com.dockyard.otpauth.exception.InvalidTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JwtAuthenticationFilter — reads the Bearer access token on each request and,
 * if it is valid, populates the Spring Security context.
 *
 * It runs once per request. If there is no token, or the token is bad, it simply
 * does NOT authenticate and lets the chain continue — the security config then
 * decides whether the target endpoint actually required authentication. This
 * keeps public endpoints (OTP request/verify, refresh, Swagger) open while
 * protecting the rest.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractBearerToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                ParsedToken parsed = jwtService.parse(token, TokenType.ACCESS);

                var authentication = new UsernamePasswordAuthenticationToken(
                        parsed.subject(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authenticated subject '{}'", parsed.subject());

            } catch (InvalidTokenException ex) {
                // Leave the context unauthenticated. Protected endpoints will then
                // 401 via the entry point; public ones proceed normally.
                log.debug("Rejected bearer token: {}", ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /** Pulls the raw token out of an "Authorization: Bearer <token>" header. */
    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(PREFIX)) {
            return header.substring(PREFIX.length()).trim();
        }
        return null;
    }
}