package com.dockyard.qrgenerator.dto;

import com.dockyard.qrgenerator.domain.ContentType;
import com.dockyard.qrgenerator.domain.ErrorCorrection;
import com.dockyard.qrgenerator.entity.QrCodeRecord;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * QrHistoryResponse — one row of generation history, safe for public exposure.
 *
 * We map from the entity so the API contract stays stable even if the table
 * changes, and so we never accidentally leak internal-only columns.
 */
@Data
@Builder
public class QrHistoryResponse {

    private Long id;
    private String content;
    private ContentType contentType;
    private int sizePx;
    private ErrorCorrection errorCorrection;
    private int byteSize;
    private LocalDateTime createdAt;

    /** Central place to convert an entity into its API representation. */
    public static QrHistoryResponse from(QrCodeRecord record) {
        return QrHistoryResponse.builder()
                .id(record.getId())
                .content(record.getContent())
                .contentType(record.getContentType())
                .sizePx(record.getSizePx())
                .errorCorrection(record.getErrorCorrection())
                .byteSize(record.getByteSize())
                .createdAt(record.getCreatedAt())
                .build();
    }
}