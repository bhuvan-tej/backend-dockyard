package com.dockyard.restapicrud.controller;

import com.dockyard.restapicrud.dto.PagedResponse;
import com.dockyard.restapicrud.dto.ProductRequest;
import com.dockyard.restapicrud.dto.ProductResponse;
import com.dockyard.restapicrud.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ProductController — HTTP layer for the Products API.
 *
 * RESPONSIBILITIES:
 *   Map HTTP requests to service methods
 *   Return correct HTTP status codes
 *   @Valid triggers Bean Validation on the request body
 *   Swagger annotations document the API automatically
 *
 * The controller is deliberately thin:
 *   No business logic — that belongs in the service
 *   No database calls — that belongs in the repository
 *   No exception handling — GlobalExceptionHandler does that
 *
 * context-path /api from application.yml + /products here
 * Full path for all endpoints: /api/products
 */
@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management API")
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products
     * Returns paginated list of all products.
     * Example: GET /api/products?page=0&size=5&sortBy=price&sortDir=desc
     */
    @GetMapping
    @Operation(summary = "Get all products", description = "Returns paginated list of all products")
    public ResponseEntity<PagedResponse<ProductResponse>> getAllProducts(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Items per page")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field to sort by")
            @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Sort direction: asc or desc")
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("GET /products page={} size={} sortBy={} sortDir={}", page, size, sortBy, sortDir);
        return ResponseEntity.ok(productService.getAllProducts(page, size, sortBy, sortDir));
    }

    /**
     * GET /api/products/{id}
     * Returns a single product by ID.
     * Returns 404 if the product does not exist.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID")
            @PathVariable Long id) {

        log.info("GET /products/{}", id);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * POST /api/products
     * Creates a new product.
     * @Valid triggers Bean Validation — returns 400 with field errors if invalid.
     * Returns 201 Created with the saved product.
     */
    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request) {

        log.info("POST /products name={}", request.getName());
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/products/{id}
     * Updates an existing product completely.
     * Returns 404 if the product does not exist.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID")
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        log.info("PUT /products/{}", id);
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    /**
     * DELETE /api/products/{id}
     * Deletes a product by ID.
     * Returns 204 No Content — no body needed after a successful delete.
     * Returns 404 if the product does not exist.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID")
            @PathVariable Long id) {

        log.info("DELETE /products/{}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/products/category/{category}
     * Returns all products in a specific category with pagination.
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<PagedResponse<ProductResponse>> getProductsByCategory(
            @Parameter(description = "Category name")
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /products/category/{}", category);
        return ResponseEntity.ok(productService.getProductsByCategory(category, page, size));
    }

    /**
     * GET /api/products/search?keyword=laptop
     * Full-text search across product name and description.
     * Case-insensitive — "laptop" matches "Laptop".
     */
    @GetMapping("/search")
    @Operation(summary = "Search products by keyword")
    public ResponseEntity<PagedResponse<ProductResponse>> searchProducts(
            @Parameter(description = "Search keyword")
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /products/search keyword={}", keyword);
        return ResponseEntity.ok(productService.searchProducts(keyword, page, size));
    }

    /**
     * GET /api/products/in-stock
     * Returns all products with stock greater than zero.
     */
    @GetMapping("/in-stock")
    @Operation(summary = "Get in-stock products")
    public ResponseEntity<PagedResponse<ProductResponse>> getInStockProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /products/in-stock");
        return ResponseEntity.ok(productService.getInStockProducts(page, size));
    }

}