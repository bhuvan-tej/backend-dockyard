package com.dockyard.qrgenerator.exception;

/**
 * QrGenerationException — thrown when the QR engine fails to encode the input.
 *
 * Typically, means the content is too large to fit in a QR code even at the
 * lowest error-correction level. Mapped to 422 Unprocessable Entity: the
 * request was well-formed but the data cannot be represented as a QR code.
 */
public class QrGenerationException extends RuntimeException {
    public QrGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}