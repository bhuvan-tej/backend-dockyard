package com.dockyard.springdockerapp.repository;

import com.dockyard.springdockerapp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProductRepository.java
 *
 * Spring Data JPA repository for the Product entity.
 *
 * By extending JpaRepository we get all standard CRUD operations
 * for free without writing any SQL or implementation code:
 *   save()        insert or update a product
 *   findById()    find a product by its ID
 *   findAll()     get all products
 *   deleteById()  delete a product by ID
 *   count()       count total products
 *   existsById()  check if a product exists
 *
 * Spring Data JPA automatically generates the implementation
 * at startup by reading the method names.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Spring Data JPA generates the SQL for this method automatically
    // by reading the method name — findBy + Name means WHERE name = ?
    // Returns Optional because the product may or may not exist
    Optional<Product> findByName(String name);

    // Find all products where price is less than or equal to the given value
    // Generated SQL: SELECT * FROM products WHERE price <= ?
    List<Product> findByPriceLessThanEqual(Double maxPrice);

    // Find all products where stock is greater than zero
    // Generated SQL: SELECT * FROM products WHERE stock > 0
    List<Product> findByStockGreaterThan(Integer minStock);
}