package com.dockyard.qrgenerator.controller;

import com.dockyard.qrgenerator.dto.*;
import com.dockyard.qrgenerator.service.GeneratedQr;
import com.dockyard.qrgenerator.service.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Base64;

/**
 * QrCodeController — HTTP layer for the QR API.
 *
 * Full base path is /api/qrcodes (context-path /api + this mapping).
 *
 * TWO WAYS TO GET AN IMAGE — because clients differ:
 *   /image  → returns raw image/png. Point an <img src> straight at it,
 *             or open it in a browser. Great for humans and simple pages.
 *   (root)  → returns JSON with a Base64 data URI + metadata. Ideal for
 *             SPAs/mobile apps that want the image AND the history id in one call.
 *
 * The controller stays thin: validate, delegate to the service, shape the HTTP
 * response. No business logic, no ZXing, no persistence here.
 */
@Slf4j
@RestController
@RequestMapping("/qrcodes")
@RequiredArgsConstructor
@Tag(name = "QR Codes", description = "Generate, decode and analyse QR codes")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    /**
     * POST /api/qrcodes/image
     * Generates a QR code and returns it as a raw PNG image.
     */
    @PostMapping(value = "/image", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Generate a QR code as a PNG image",
            description = "Returns raw image/png. Use this when you want to display or download the image directly.")
    public ResponseEntity<byte[]> generateImage(@Valid @RequestBody QrCodeRequest request) {
        log.info("POST /qrcodes/image");
        GeneratedQr generated = qrCodeService.generate(request);
        return pngResponse(generated.png());
    }

    /**
     * POST /api/qrcodes
     * Generates a QR code and returns JSON with an embeddable Base64 data URI.
     */
    @PostMapping
    @Operation(summary = "Generate a QR code as JSON (Base64 data URI)",
            description = "Returns the image as a data URI plus metadata and a history id. Ideal for web/mobile apps.")
    public ResponseEntity<QrCodeResponse> generateJson(@Valid @RequestBody QrCodeRequest request) {
        log.info("POST /qrcodes");
        GeneratedQr generated = qrCodeService.generate(request);
        return ResponseEntity.ok(toResponse(generated));
    }

    /**
     * POST /api/qrcodes/wifi
     * Convenience endpoint: build a WiFi-join QR from friendly fields.
     */
    @PostMapping(value = "/wifi", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Generate a WiFi network-join QR code",
            description = "Scanning the returned code connects the phone to the network with no typing.")
    public ResponseEntity<byte[]> generateWifi(@Valid @RequestBody WifiQrRequest request) {
        log.info("POST /qrcodes/wifi ssid={}", request.getSsid());
        GeneratedQr generated = qrCodeService.generateWifi(request);
        return pngResponse(generated.png());
    }

    /**
     * POST /api/qrcodes/vcard
     * Convenience endpoint: build a vCard contact QR from friendly fields.
     */
    @PostMapping(value = "/vcard", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Generate a vCard contact QR code",
            description = "Scanning the returned code opens a pre-filled 'add contact' screen.")
    public ResponseEntity<byte[]> generateVCard(@Valid @RequestBody VCardQrRequest request) {
        log.info("POST /qrcodes/vcard name={}", request.getFullName());
        GeneratedQr generated = qrCodeService.generateVCard(request);
        return pngResponse(generated.png());
    }

    /**
     * POST /api/qrcodes/decode  (multipart/form-data)
     * Reads a QR code out of an uploaded image and returns the original text.
     */
    @PostMapping(value = "/decode", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Decode a QR code from an uploaded image",
            description = "Upload a PNG/JPG containing a QR code to recover its text.")
    public ResponseEntity<QrDecodeResponse> decode(
            @Parameter(description = "Image file containing a QR code")
            @RequestParam("file") MultipartFile file) {
        log.info("POST /qrcodes/decode file={}", file.getOriginalFilename());
        return ResponseEntity.ok(qrCodeService.decode(file));
    }

    /**
     * GET /api/qrcodes/history
     * Paginated list of previously generated QR codes, newest first.
     */
    @GetMapping("/history")
    @Operation(summary = "List generation history (paginated)")
    public ResponseEntity<PagedResponse<QrHistoryResponse>> history(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page")
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /qrcodes/history page={} size={}", page, size);
        return ResponseEntity.ok(qrCodeService.getHistory(page, size));
    }

    /**
     * GET /api/qrcodes/analytics
     * Aggregated stats: total generated and a breakdown by content type.
     */
    @GetMapping("/analytics")
    @Operation(summary = "Get usage analytics")
    public ResponseEntity<QrAnalyticsResponse> analytics() {
        log.info("GET /qrcodes/analytics");
        return ResponseEntity.ok(qrCodeService.getAnalytics());
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /** Wraps PNG bytes with the right content type and a short cache header. */
    private ResponseEntity<byte[]> pngResponse(byte[] png) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                // QR images are deterministic, so letting clients cache is safe.
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .body(png);
    }

    /** Maps a service result into the JSON response with a Base64 data URI. */
    private QrCodeResponse toResponse(GeneratedQr generated) {
        String base64 = Base64.getEncoder().encodeToString(generated.png());
        return QrCodeResponse.builder()
                .id(generated.qrRecord().getId())
                .content(generated.qrRecord().getContent())
                .contentType(generated.qrRecord().getContentType())
                .dataUri("data:image/png;base64," + base64)
                .size(generated.qrRecord().getSizePx())
                .byteSize(generated.qrRecord().getByteSize())
                .createdAt(generated.qrRecord().getCreatedAt())
                .build();
    }
}