package com.dockyard.restapicrud.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * PagedResponse — wraps a paginated list of items with metadata.
 *
 * WHY PAGINATION?
 *
 * Without pagination:
 *   GET /products → returns ALL products
 *   100 products   = fine
 *   100,000 products = slow response, huge payload, out of memory risk
 *
 * With pagination:
 *   GET /products?page=0&size=10 → returns first 10 products
 *   GET /products?page=1&size=10 → returns next 10 products
 *   Response always stays small regardless of total count
 *
 * The metadata tells the client:
 *   - How many items are in this page
 *   - Total items across all pages
 *   - Total number of pages
 *   - Whether there are more pages
 *
 * Generic type T means this works for any response type:
 *   PagedResponse<ProductResponse>
 *   PagedResponse<OrderResponse>
 */
@Data
@Builder
public class PagedResponse<T> {

    // The items for this page
    private List<T> content;

    // Current page number (0-based)
    private int page;

    // Number of items requested per page
    private int size;

    // Total number of items across ALL pages
    private long totalElements;

    // Total number of pages
    private int totalPages;

    // True if there are no more pages after this one
    private boolean last;

    /**
     * Static factory method — creates PagedResponse from Spring's Page object.
     * Spring Data JPA returns a Page<T> from repository methods.
     * This converts it to our API response format.
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}

