package com.dockyard.qrgenerator.service;

import com.dockyard.qrgenerator.entity.QrCodeRecord;

/**
 * GeneratedQr — the service's internal result of a generation call.
 *
 * Carries BOTH the raw PNG bytes (for the image endpoint) and the persisted
 * history qrRecord (for the JSON endpoint and analytics). The controller decides
 * which representation to return, keeping the service free of HTTP concerns.
 *
 * @param png    the rendered QR image bytes
 * @param qrRecord the saved history qrRecord, with its generated id and timestamp
 */
public record GeneratedQr(byte[] png, QrCodeRecord qrRecord) {
}