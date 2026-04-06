package com.dockyard.restapicrud.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ProductResponse — the shape of data we send back to the client.
 *
 * WHY A SEPARATE RESPONSE DTO?
 *
 * Returning the entity directly causes problems:
 *   - Exposes all database fields including internal ones
 *   - Jackson may trigger lazy loading of relationships
 *   - You cannot control exactly what the client sees
 *   - Adding a field to the entity automatically exposes it in the API
 *
 * With a response DTO:
 *   - You explicitly choose which fields to expose
 *   - API contract is stable even if the entity changes
 *   - You can add computed fields (like isInStock) without database columns
 */
@Data
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String category;

    // Computed field — not stored in the database
    // Derived from the stock value
    // Client does not need to check stock > 0 themselves
    private Boolean inStock;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Static factory method to convert a Product entity to a ProductResponse.
     * Keeps conversion logic in one place — not scattered across services.
     */
    public static ProductResponse from(com.dockyard.restapicrud.entity.Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                // Computed from stock — true if stock > 0
                .inStock(product.getStock() > 0)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

}