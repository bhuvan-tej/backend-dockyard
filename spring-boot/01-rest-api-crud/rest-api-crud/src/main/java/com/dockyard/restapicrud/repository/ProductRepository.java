package com.dockyard.restapicrud.repository;

import com.dockyard.restapicrud.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ProductRepository — all database operations for Product.
 *
 * Extends JpaRepository<Product, Long>:
 *   Product = the entity type
 *   Long    = the type of the primary key (id field)
 *
 * JpaRepository gives us for free:
 *   save(product)          → INSERT or UPDATE
 *   findById(id)           → SELECT WHERE id = ?
 *   findAll()              → SELECT all
 *   findAll(pageable)      → SELECT with LIMIT OFFSET ORDER BY
 *   deleteById(id)         → DELETE WHERE id = ?
 *   existsById(id)         → SELECT COUNT WHERE id = ?
 *   count()                → SELECT COUNT(*)
 *
 * Custom methods below are generated from method names by Spring Data JPA.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find a product by exact name match.
     * Generated SQL: SELECT * FROM products WHERE name = ?
     * Returns Optional because the product may or may not exist.
     */
    Optional<Product> findByName(String name);

    /**
     * Find all products in a category with pagination.
     * Generated SQL: SELECT * FROM products WHERE category = ? LIMIT ? OFFSET ?
     *
     * Pageable carries: page number, page size, sort direction
     * Page<Product> carries: content list + pagination metadata
     */
    Page<Product> findByCategory(String category, Pageable pageable);

    /**
     * Full-text search across name and description.
     *
     * Uses @Query with JPQL (Java Persistence Query Language).
     * JPQL uses entity class names and field names, not table/column names.
     * LOWER() makes the search case-insensitive.
     * LIKE %:keyword% matches anywhere in the string.
     *
     * @Param("keyword") binds the method parameter to the :keyword placeholder.
     */
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Check if a product with this name already exists.
     * Used before creating a new product to give a better error than
     * a database unique constraint violation.
     * Generated SQL: SELECT COUNT(*) > 0 FROM products WHERE name = ?
     */
    boolean existsByName(String name);

    /**
     * Find all products with stock greater than zero.
     * Generated SQL: SELECT * FROM products WHERE stock > 0 LIMIT ? OFFSET ?
     */
    Page<Product> findByStockGreaterThan(Integer minStock, Pageable pageable);
}