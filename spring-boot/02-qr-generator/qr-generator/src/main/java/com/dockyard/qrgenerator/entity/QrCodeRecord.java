package com.dockyard.qrgenerator.entity;

import com.dockyard.qrgenerator.domain.ContentType;
import com.dockyard.qrgenerator.domain.ErrorCorrection;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * QrCodeRecord — one row per QR code we generate.
 *
 * We deliberately do NOT store the PNG bytes in the database. QR images are
 * cheap and deterministic — the exact same image can be regenerated from the
 * content + options at any time. Storing bytes would bloat the DB for no gain.
 * Instead we store the metadata, which is what analytics actually needs.
 *
 * Lombok:
 *   @Getter/@Setter      → accessors
 *   @Builder             → QrCodeRecord.builder()...build()
 *   @NoArgsConstructor   → required by JPA
 *   @AllArgsConstructor  → required by @Builder
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "qr_codes")
public class QrCodeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The raw payload that was encoded into the QR code. */
    @Column(nullable = false, length = 2048)
    private String content;

    /** Heuristic classification (URL, WIFI, VCARD…) used for analytics. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ContentType contentType;

    /** Width/height of the generated image in pixels (QR codes are square). */
    @Column(nullable = false)
    private Integer sizePx;

    /** Reed-Solomon error correction level used when encoding. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private ErrorCorrection errorCorrection;

    /** Size of the produced PNG in bytes — handy for spotting oversized codes. */
    @Column(nullable = false)
    private Integer byteSize;

    /** Set automatically by Hibernate on INSERT. */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

}