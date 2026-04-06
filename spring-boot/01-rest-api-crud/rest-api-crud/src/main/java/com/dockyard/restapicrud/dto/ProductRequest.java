package com.dockyard.restapicrud.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * ProductRequest — the shape of data the client sends to create or update a product.
 *
 * WHY A SEPARATE DTO INSTEAD OF USING THE ENTITY DIRECTLY?
 *
 * Using the entity directly as a request body causes problems:
 *   - Client can send id, createdAt, updatedAt — fields they should not control
 *   - Validation annotations mix with persistence annotations on the entity
 *   - API shape is tied to database schema — changing DB breaks the API
 *
 * With a separate DTO:
 *   - Client can only send the fields we explicitly define here
 *   - Validation is clean and separate from persistence concerns
 *   - API can evolve independently of the database schema
 */
@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private Double price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @NotBlank(message = "Category is required")
    private String category;

}