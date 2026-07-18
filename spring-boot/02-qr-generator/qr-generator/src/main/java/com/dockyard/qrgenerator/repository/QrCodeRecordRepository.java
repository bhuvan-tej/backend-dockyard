package com.dockyard.qrgenerator.repository;

import com.dockyard.qrgenerator.domain.ContentType;
import com.dockyard.qrgenerator.entity.QrCodeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * QrCodeRecordRepository — database access for QR history.
 *
 * Spring Data JPA generates the implementation at runtime. We add:
 *   - a derived query for filtering history by content type
 *   - a JPQL aggregate for the analytics breakdown (one query, not N)
 */
public interface QrCodeRecordRepository extends JpaRepository<QrCodeRecord, Long> {

    /** History filtered by a single content type, still paginated. */
    Page<QrCodeRecord> findByContentType(ContentType contentType, Pageable pageable);

    /**
     * Analytics in a single round-trip: how many QR codes per content type.
     * Returns rows of [ContentType, count] — the service turns this into a map.
     */
    @Query("""
            SELECT q.contentType, COUNT(q)
            FROM QrCodeRecord q
            GROUP BY q.contentType
            ORDER BY COUNT(q) DESC
            """)
    List<Object[]> countGroupedByContentType();
}