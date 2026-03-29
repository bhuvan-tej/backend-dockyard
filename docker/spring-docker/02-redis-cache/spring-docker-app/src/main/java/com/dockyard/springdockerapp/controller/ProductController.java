package com.dockyard.springdockerapp.controller;

import com.dockyard.springdockerapp.entity.Product;
import com.dockyard.springdockerapp.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ProductController.java
 *
 * REST controller that exposes HTTP endpoints for product operations.
 * All endpoints are under the /api/products base path.
 *
 * @RestController combines @Controller and @ResponseBody
 * It means every method returns data directly as JSON
 * instead of rendering a view template
 *
 * @RequestMapping sets the base URL path for all methods in this class
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    // Spring injects ProductService via constructor injection
    private final ProductService productService;

    // GET /api/products
    // Returns all products in the database as a JSON array
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("GET /api/products");
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // GET /api/products/{id}
    // Returns a single product by ID
    // {id} is a path variable — it is part of the URL itself
    // For example GET /api/products/1 returns the product with id 1
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        log.info("GET /api/products/{}", id);
        // map() returns 200 OK with the product if found
        // orElse() returns 404 Not Found if the product does not exist
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/products
    // Creates a new product from the JSON body of the request
    // @RequestBody tells Spring to parse the JSON body into a Product object
    // Returns 201 Created with the saved product including its generated ID
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        log.info("POST /api/products - creating: {}", product.getName());
        Product saved = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /api/products/{id}
    // Updates an existing product with the data from the request body
    // Returns 200 OK with updated product, or 404 if product not found
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id,
                                                 @RequestBody Product product) {
        log.info("PUT /api/products/{}", id);
        return productService.updateProduct(id, product)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/products/{id}
    // Deletes a product by ID
    // Returns 204 No Content if deleted, 404 if not found
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("DELETE /api/products/{}", id);
        if (productService.deleteProduct(id)) {
            // 204 No Content is the standard response for a successful delete
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // GET /api/products/search?maxPrice=100
    // Returns all products under a given price
    // @RequestParam reads the maxPrice value from the query string
    @GetMapping("/search")
    public ResponseEntity<List<Product>> getProductsByMaxPrice(
            @RequestParam Double maxPrice) {
        log.info("GET /api/products/search?maxPrice={}", maxPrice);
        return ResponseEntity.ok(productService.getProductsByMaxPrice(maxPrice));
    }

    // GET /api/products/in-stock
    // Returns all products that have stock greater than zero
    @GetMapping("/in-stock")
    public ResponseEntity<List<Product>> getInStockProducts() {
        log.info("GET /api/products/in-stock");
        return ResponseEntity.ok(productService.getInStockProducts());
    }
}