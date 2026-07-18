package com.dockyard.qrgenerator.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler — one place that turns every exception into a clean
 * {@link ErrorResponse}. Controllers and services stay free of try/catch noise.
 *
 * Status-code map:
 *   MethodArgumentNotValidException  → 400  (bad field values)
 *   QrDecodingException              → 400  (unreadable image)
 *   MaxUploadSizeExceededException   → 413  (upload too big)
 *   QrGenerationException            → 422  (valid input, impossible to encode)
 *   Exception (catch-all)            → 500  (unexpected — logged with stack trace)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Bean Validation failures — collect every field error into a map. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            fieldErrors.put(field, error.getDefaultMessage());
        });

        log.warn("Validation failed for {}: {}", request.getRequestURI(), fieldErrors);

        return build(HttpStatus.BAD_REQUEST, "Validation Failed",
                "Request validation failed", request, fieldErrors);
    }

    /** Uploaded image could not be read as a QR code. */
    @ExceptionHandler(QrDecodingException.class)
    public ResponseEntity<ErrorResponse> handleDecoding(
            QrDecodingException ex, HttpServletRequest request) {

        log.warn("Decode failed at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Decode Failed", ex.getMessage(), request, null);
    }

    /** Upload larger than the configured multipart limit. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleTooLarge(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {

        log.warn("Upload too large at {}", request.getRequestURI());
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "Payload Too Large",
                "Uploaded file exceeds the maximum allowed size", request, null);
    }

    /** Content was valid but simply cannot fit into a QR code. */
    @ExceptionHandler(QrGenerationException.class)
    public ResponseEntity<ErrorResponse> handleGeneration(
            QrGenerationException ex, HttpServletRequest request) {

        log.warn("Generation failed at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity",
                ex.getMessage(), request, null);
    }

    /** Safety net — nothing unexpected reaches the client raw. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", request, null);
    }

    /** Shared builder so every handler produces an identically shaped response. */
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error,
                                                String message, HttpServletRequest request,
                                                Map<String, String> fieldErrors) {
        ErrorResponse body = ErrorResponse.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(fieldErrors)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}