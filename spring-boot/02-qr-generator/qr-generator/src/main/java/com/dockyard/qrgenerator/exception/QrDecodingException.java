package com.dockyard.qrgenerator.exception;

/**
 * QrDecodingException — thrown when an uploaded image cannot be read as a QR code.
 *
 * Causes: the file is not an image, the image contains no QR code, or the code
 * is too blurry/damaged to decode. Mapped to 400 Bad Request — the client sent
 * something we could not process.
 */
public class QrDecodingException extends RuntimeException {
    public QrDecodingException(String message) {
        super(message);
    }

    public QrDecodingException(String message, Throwable cause) {
        super(message, cause);
    }
}