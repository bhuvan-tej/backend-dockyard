package com.dockyard.qrgenerator.dto;

import com.dockyard.qrgenerator.domain.ContentType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * QrAnalyticsResponse — an at-a-glance summary of everything generated so far.
 *
 * Turns the raw history table into something you would actually put on a
 * dashboard: total volume and a breakdown by what the codes were used for.
 */
@Data
@Builder
public class QrAnalyticsResponse {

    /** Total number of QR codes generated since startup. */
    private long totalGenerated;

    /** Count per content type, e.g. { "URL": 42, "WIFI": 13 }. */
    private Map<ContentType, Long> byContentType;

    /** The single most-generated content type, or null if history is empty. */
    private ContentType mostCommonType;
}