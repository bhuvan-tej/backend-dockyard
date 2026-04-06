package com.dockyard.restapicrud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ResourceNotFoundException — thrown when a requested resource does not exist.
 *
 * Examples:
 *   GET /products/999  → product 999 does not exist → throw this
 *   PUT /products/999  → product 999 does not exist → throw this
 *   DELETE /products/999 → product 999 does not exist → throw this
 *
 * @ResponseStatus(HttpStatus.NOT_FOUND) tells Spring to return 404
 * when this exception is thrown and not caught by anything else.
 *
 * The GlobalExceptionHandler catches it and returns a consistent ErrorResponse.
 *
 * extends RuntimeException — unchecked exception
 * No need to declare it in method signatures with throws
 * Spring catches it automatically via @ExceptionHandler
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * @param resourceName  the type of resource e.g. "Product"
     * @param fieldName     the field used to look it up e.g. "id"
     * @param fieldValue    the value that was not found e.g. 999
     *
     * Produces message: "Product not found with id: 999"
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
    }
}