package com.dockyard.restapicrud.service;

import com.dockyard.restapicrud.dto.PagedResponse;
import com.dockyard.restapicrud.dto.ProductRequest;
import com.dockyard.restapicrud.dto.ProductResponse;
import com.dockyard.restapicrud.entity.Product;
import com.dockyard.restapicrud.exception.ResourceNotFoundException;
import com.dockyard.restapicrud.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProductService — all business logic for products.
 *
 * RESPONSIBILITIES:
 *   Convert ProductRequest DTO to Product entity
 *   Call repository for database operations
 *   Convert Product entity to ProductResponse DTO
 *   Throw domain exceptions when business rules are violated
 *
 * @Transactional wraps each method in a database transaction
 *   If anything fails mid-method the entire transaction rolls back
 *   readOnly = true on read methods — database can optimise queries
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Get all products with pagination and sorting.
     *
     * page    → which page number (0-based, so page 0 = first page)
     * size    → how many items per page
     * sortBy  → which field to sort on (name, price, createdAt etc)
     * sortDir → ASC or DESC
     */
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getAllProducts(
            int page, int size, String sortBy, String sortDir) {

        log.info("Fetching products page:{} size:{} sort:{} {}", page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductResponse> productPage = productRepository
                .findAll(pageable)
                .map(ProductResponse::from);

        return PagedResponse.from(productPage);
    }

    /**
     * Get a single product by ID.
     * Throws ResourceNotFoundException if not found.
     * GlobalExceptionHandler converts it to a 404 response.
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.info("Fetching product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        return ProductResponse.from(product);
    }

    /**
     * Create a new product.
     * Maps ProductRequest to a Product entity, saves it, returns ProductResponse.
     * We never set id, createdAt or updatedAt — Hibernate handles those.
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating product: {}", request.getName());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created with id: {}", saved.getId());

        return ProductResponse.from(saved);
    }

    /**
     * Update an existing product.
     * Fetches existing entity, updates its fields, saves it.
     * Hibernate detects the changes and runs UPDATE automatically.
     */
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Updating product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());

        Product updated = productRepository.save(product);
        log.info("Product updated: {}", updated.getId());

        return ProductResponse.from(updated);
    }

    /**
     * Delete a product by ID.
     * Checks it exists first — gives a clean 404 instead of silently doing nothing.
     */
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }

        productRepository.deleteById(id);
        log.info("Product deleted: {}", id);
    }

    /**
     * Get all products in a specific category with pagination.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getProductsByCategory(
            String category, int page, int size) {

        log.info("Fetching products in category: {}", category);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<ProductResponse> productPage = productRepository
                .findByCategory(category, pageable)
                .map(ProductResponse::from);

        return PagedResponse.from(productPage);
    }

    /**
     * Search products by keyword across name and description fields.
     * Case-insensitive — "laptop" matches "Laptop" and "LAPTOP".
     */
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> searchProducts(
            String keyword, int page, int size) {

        log.info("Searching products with keyword: {}", keyword);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<ProductResponse> productPage = productRepository
                .searchByKeyword(keyword, pageable)
                .map(ProductResponse::from);

        return PagedResponse.from(productPage);
    }

    /**
     * Get all products that have stock greater than zero.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getInStockProducts(int page, int size) {

        log.info("Fetching in-stock products");

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<ProductResponse> productPage = productRepository
                .findByStockGreaterThan(0, pageable)
                .map(ProductResponse::from);

        return PagedResponse.from(productPage);
    }
}