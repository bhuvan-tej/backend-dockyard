package com.dockyard.qrgenerator.service;

import com.dockyard.qrgenerator.domain.ContentType;
import com.dockyard.qrgenerator.dto.*;
import com.dockyard.qrgenerator.engine.QrCodeEngine;
import com.dockyard.qrgenerator.engine.QrPayloadBuilder;
import com.dockyard.qrgenerator.entity.QrCodeRecord;
import com.dockyard.qrgenerator.exception.QrDecodingException;
import com.dockyard.qrgenerator.repository.QrCodeRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.EnumMap;
import java.util.Map;

/**
 * QrCodeService — all business logic for the QR API.
 *
 * RESPONSIBILITIES:
 *   Delegate encode/decode to {@link QrCodeEngine} (the only ZXing consumer)
 *   Build WiFi/vCard payloads via {@link QrPayloadBuilder}
 *   Classify content and qrRecord every generation for history + analytics
 *   Expose paginated history and an aggregated analytics view
 *
 * The service never touches ZXing or HTTP types — it sits cleanly between the
 * controller (HTTP) and the engine/repository (infrastructure).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final QrCodeEngine engine;
    private final QrPayloadBuilder payloadBuilder;
    private final QrCodeRecordRepository repository;

    /**
     * Generates a QR code from a free-form request and records it.
     * Returns both the PNG bytes and the saved history row.
     */
    @Transactional
    public GeneratedQr generate(QrCodeRequest request) {
        log.info("Generating QR: {} chars, {}px, EC={}",
                request.getContent().length(), request.getSize(), request.getErrorCorrection());

        byte[] png = engine.generatePng(
                request.getContent(),
                request.getSize(),
                request.getMargin(),
                request.getForegroundColor(),
                request.getBackgroundColor(),
                request.getErrorCorrection());

        QrCodeRecord record = save(request.getContent(), request.getSize(),
                request.getErrorCorrection(), png.length);

        return new GeneratedQr(png, record);
    }

    /**
     * Generates a WiFi-join QR code. The friendly request is turned into the
     * standard {@code WIFI:...} payload, then generated with default styling.
     */
    @Transactional
    public GeneratedQr generateWifi(WifiQrRequest request) {
        String payload = payloadBuilder.buildWifi(request);
        log.info("Generating WiFi QR for SSID '{}'", request.getSsid());

        byte[] png = engine.generatePng(payload, request.getSize(), 1,
                "#000000", "#FFFFFF", request.getErrorCorrection());

        QrCodeRecord record = save(payload, request.getSize(),
                request.getErrorCorrection(), png.length);

        return new GeneratedQr(png, record);
    }

    /**
     * Generates a vCard contact QR code from friendly contact fields.
     */
    @Transactional
    public GeneratedQr generateVCard(VCardQrRequest request) {
        String payload = payloadBuilder.buildVCard(request);
        log.info("Generating vCard QR for '{}'", request.getFullName());

        byte[] png = engine.generatePng(payload, request.getSize(), 1,
                "#000000", "#FFFFFF", request.getErrorCorrection());

        QrCodeRecord record = save(payload, request.getSize(),
                request.getErrorCorrection(), png.length);

        return new GeneratedQr(png, record);
    }

    /**
     * Reads a QR code out of an uploaded image and returns the text.
     * Decoding is read-only — we do not qrRecord scans in history.
     */
    public QrDecodeResponse decode(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new QrDecodingException("No image file was uploaded");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception ex) {
            throw new QrDecodingException("Could not read the uploaded file", ex);
        }

        String content = engine.decode(bytes);
        log.info("Decoded QR: {} chars", content.length());

        return QrDecodeResponse.builder()
                .content(content)
                .contentType(ContentType.classify(content))
                .format("QR_CODE")
                .build();
    }

    /** Paginated history, newest first. */
    @Transactional(readOnly = true)
    public PagedResponse<QrHistoryResponse> getHistory(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<QrCodeRecord> records = repository.findAll(pageable);
        return PagedResponse.from(records, QrHistoryResponse::from);
    }

    /**
     * Aggregated analytics over the whole history: total volume, a per-type
     * breakdown, and the single most-common content type.
     */
    @Transactional(readOnly = true)
    public QrAnalyticsResponse getAnalytics() {
        long total = repository.count();

        // Preserve enum order and guarantee every type appears (even with 0).
        Map<ContentType, Long> breakdown = new EnumMap<>(ContentType.class);
        ContentType mostCommon = null;
        long highest = -1;

        for (Object[] row : repository.countGroupedByContentType()) {
            ContentType type = (ContentType) row[0];
            long count = (Long) row[1];
            breakdown.put(type, count);
            if (count > highest) {
                highest = count;
                mostCommon = type;
            }
        }

        return QrAnalyticsResponse.builder()
                .totalGenerated(total)
                .byContentType(breakdown)
                .mostCommonType(mostCommon)
                .build();
    }

    /** Classifies, builds and persists a history qrRecord in one place. */
    private QrCodeRecord save(String content, int size,
                             com.dockyard.qrgenerator.domain.ErrorCorrection ec, int byteSize) {
        QrCodeRecord record = QrCodeRecord.builder()
                .content(content)
                .contentType(ContentType.classify(content))
                .sizePx(size)
                .errorCorrection(ec)
                .byteSize(byteSize)
                .build();
        return repository.save(record);
    }
}