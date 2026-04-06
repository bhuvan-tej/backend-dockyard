package com.dockyard.restapicrud.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Product entity — maps to the "products" table in PostgreSQL.
 *
 * Lombok annotations:
 *   @Data           → getters, setters, equals, hashCode, toString
 *   @Builder        → builder pattern: Product.builder().name("x").build()
 *   @NoArgsConstructor → required by JPA
 *   @AllArgsConstructor → required by @Builder
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(nullable = false) — database level constraint
    // @NotBlank — validation level constraint (checked before hitting DB)
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false, unique = true)
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    // @NotNull — price must be provided
    // @Positive — price must be greater than zero
    // @DecimalMin — sets the minimum value with a readable message
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    @Column(nullable = false)
    private Double price;

    // Stock must be zero or positive — cannot have negative stock
    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    @Column(nullable = false)
    private Integer stock;

    @NotBlank(message = "Category is required")
    @Column(nullable = false)
    private String category;

    // @CreationTimestamp — Hibernate sets this automatically on INSERT
    // updatable = false — this column never changes after creation
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // @UpdateTimestamp — Hibernate updates this automatically on UPDATE
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

}