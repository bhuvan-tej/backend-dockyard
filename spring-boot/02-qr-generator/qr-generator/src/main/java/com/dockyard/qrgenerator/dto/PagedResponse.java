package com.dockyard.qrgenerator.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * PagedResponse — a clean, transport-friendly pagination wrapper.
 *
 * Spring Data's {@link Page} serializes to a large, unstable JSON structure.
 * This exposes only the fields clients need, keeping the API contract stable.
 *
 * @param <T> the element type of the page content
 */
@Data
@Builder
public class PagedResponse<T> {

    /** The items on this page. */
    private List<T> content;

    /** Zero-based page number. */
    private int page;

    /** Requested page size. */
    private int size;

    /** Total number of elements across all pages. */
    private long totalElements;

    /** Total number of pages. */
    private int totalPages;

    /** True if this is the last page. */
    private boolean last;

    /**
     * Builds a PagedResponse from a Spring Data Page, mapping each entity to a
     * DTO via the supplied function so the caller controls the output shape.
     */
    public static <E, T> PagedResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return PagedResponse.<T>builder()
                .content(page.getContent().stream().map(mapper).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}