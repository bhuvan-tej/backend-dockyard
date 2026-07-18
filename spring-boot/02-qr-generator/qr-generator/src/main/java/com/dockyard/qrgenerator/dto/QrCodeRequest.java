package com.dockyard.qrgenerator.dto;

import com.dockyard.qrgenerator.domain.ErrorCorrection;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * QrCodeRequest — everything a client can control when generating a QR code.
 *
 * Only {@code content} is required. Every visual option has a sensible default
 * so the simplest possible request is just: { "content": "https://x.com" }.
 *
 * Validation runs BEFORE any encoding happens, so bad input never reaches
 * the ZXing engine — it fails fast with a clear 400 and a field-level message.
 */
@Data
public class QrCodeRequest {

    /**
     * The text to encode. QR (version 40, level L) tops out around 2953 bytes,
     * so we cap well below that to guarantee the payload always fits.
     */
    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Content cannot exceed 2000 characters")
    private String content;

    /** Image width/height in pixels. QR codes are square, so one value covers both. */
    @Min(value = 50, message = "Size must be at least 50px")
    @Max(value = 2000, message = "Size cannot exceed 2000px")
    private int size = 300;

    /**
     * Quiet-zone width around the code, measured in modules (QR "pixels").
     * The QR spec recommends at least 4; 1–2 is fine on screens.
     */
    @Min(value = 0, message = "Margin cannot be negative")
    @Max(value = 50, message = "Margin cannot exceed 50 modules")
    private int margin = 1;

    /** Colour of the dark modules, as a #RRGGBB hex string. */
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "foregroundColor must be a #RRGGBB hex value")
    private String foregroundColor = "#000000";

    /** Colour of the light background, as a #RRGGBB hex string. */
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "backgroundColor must be a #RRGGBB hex value")
    private String backgroundColor = "#FFFFFF";

    /** Error-correction level. Defaults to M (~15% recovery), the common choice. */
    @NotNull(message = "errorCorrection is required")
    private ErrorCorrection errorCorrection = ErrorCorrection.M;
}