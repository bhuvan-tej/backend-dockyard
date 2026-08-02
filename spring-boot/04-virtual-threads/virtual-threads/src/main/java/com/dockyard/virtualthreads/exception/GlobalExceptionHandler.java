package com.dockyard.virtualthreads.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.LocalDateTime;

/**
 * GlobalExceptionHandler — turns validation failures (e.g. an out-of-range
 * {@code tasks} or {@code delayMs} query param) and any unexpected failure
 * into the same consistent {@link ErrorResponse} shape.
 *
 * NOTE: two different validation exceptions can surface here depending on
 * how Spring validates {@code @RequestParam}s: {@link HandlerMethodValidationException}
 * (the newer, Spring MVC-native path) or {@link ConstraintViolationException}
 * (the older AOP-proxy path, triggered by {@code @Validated} on the
 * controller class, as used on {@code ThreadDemoController}). Both are
 * handled the same way here.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Thrown when a @Min/@Max-annotated @RequestParam fails validation (Spring MVC-native path). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleParamValidation(
            HandlerMethodValidationException ex, HttpServletRequest request) {

        log.warn("Invalid query parameter at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Validation Failed",
                "One or more query parameters are out of the allowed range", request);
    }

    /** Thrown by the AOP method-validation interceptor when the controller carries @Validated. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        log.warn("Invalid query parameter at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Validation Failed",
                "One or more query parameters are out of the allowed range", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        log.warn("Bad request at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error,
                                                String message, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}