package com.dockyard.qrgenerator.dto;

import com.dockyard.qrgenerator.domain.ContentType;
import lombok.Builder;
import lombok.Data;

/**
 * QrDecodeResponse — the result of reading a QR code back out of an image.
 *
 * Demonstrates the reverse direction: upload a QR photo/screenshot and get
 * the original text, plus the same content classification used everywhere else.
 */
@Data
@Builder
public class QrDecodeResponse {

    /** The text recovered from the image. */
    private String content;

    /** Classification of the decoded content. */
    private ContentType contentType;

    /** Barcode format ZXing detected — always QR_CODE here. */
    private String format;
}