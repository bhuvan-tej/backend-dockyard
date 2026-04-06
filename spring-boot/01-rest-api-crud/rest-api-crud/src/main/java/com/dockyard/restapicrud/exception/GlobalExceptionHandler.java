package com.dockyard.restapicrud.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler — catches exceptions from ALL controllers.
 *
 * WHY THIS EXISTS:
 *
 * Without a global handler:
 *   ResourceNotFoundException → Spring returns a generic 500 error
 *   Validation failure → Spring returns a 400 with a complex nested JSON
 *   Both have different formats — client cannot handle them consistently
 *
 * With GlobalExceptionHandler:
 *   Every exception is caught here
 *   Every error response has the same ErrorResponse shape
 *   Controllers stay clean — no try/catch blocks needed
 *   One place to handle all error cases
 *
 * @RestControllerAdvice — applies to all @RestController classes
 * @ExceptionHandler — marks a method to handle a specific exception type
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ResourceNotFoundException — returns 404.
     * Thrown when GET/PUT/DELETE is called with a non-existent ID.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found: {} - Path: {}", ex.getMessage(), request.getRequestURI());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles validation failures — returns 400.
     * Thrown when @Valid fails on a request body.
     *
     * Collects ALL field errors into a map so the client knows
     * exactly which fields are wrong and why — all at once.
     *
     * Example response:
     * {
     *   "status": 400,
     *   "error": "Validation Failed",
     *   "message": "Request validation failed",
     *   "errors": {
     *     "name": "must not be blank",
     *     "price": "must be greater than zero"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Collect all field errors into a map: fieldName → errorMessage
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        log.warn("Validation failed for {}: {}", request.getRequestURI(), errors);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Request validation failed")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles duplicate key violations — returns 409 Conflict.
     * Thrown when creating a product with a name that already exists.
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException ex,
            HttpServletRequest request) {

        log.warn("Data integrity violation at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message("A product with this name already exists")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Catches any other unexpected exception — returns 500.
     * This is the safety net — no unhandled exception reaches the client.
     * Logs the full stack trace for debugging.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        // Log full stack trace for unexpected errors
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                // Never expose internal error details to clients in production
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}