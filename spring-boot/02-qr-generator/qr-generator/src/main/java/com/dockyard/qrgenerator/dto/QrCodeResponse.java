package com.dockyard.qrgenerator.dto;

import com.dockyard.qrgenerator.domain.ContentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * QrCodeResponse — the JSON shape returned by the "embeddable" generate endpoint.
 *
 * Instead of raw image bytes, this returns the PNG as a Base64 data URI. That
 * string can be dropped straight into an HTML {@code <img src="...">} or a
 * mobile app with no extra HTTP request — the most common way front-ends
 * consume generated QR codes.
 */
@Data
@Builder
public class QrCodeResponse {

    /** History id — use it to look this qrRecord up later via /history. */
    private Long id;

    /** The payload that was encoded. */
    private String content;

    /** Detected classification of the content. */
    private ContentType contentType;

    /**
     * The QR image as a ready-to-embed data URI:
     * {@code data:image/png;base64,iVBORw0KGgo...}
     */
    private String dataUri;

    /** Image width/height in pixels. */
    private int size;

    /** Size of the underlying PNG in bytes. */
    private int byteSize;

    private LocalDateTime createdAt;
}