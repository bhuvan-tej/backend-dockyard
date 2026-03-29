package com.dockyard.springdockerapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product.java
 *
 * A JPA entity representing a product in the database.
 * Hibernate reads this class and creates a "product" table automatically
 * because of hibernate.ddl-auto=update in application.yml
 *
 * Lombok annotations remove boilerplate:
 *   @Data           generates getters, setters, equals, hashCode, toString
 *   @Builder        generates a builder pattern for creating objects
 *   @NoArgsConstructor  generates a no-arg constructor (required by JPA)
 *   @AllArgsConstructor generates a constructor with all fields
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
// @Table maps this entity to the "products" table in PostgreSQL
// If not specified the table name defaults to the class name in lowercase
@Table(name = "products")
public class Product {

    // @Id marks this field as the primary key
    // @GeneratedValue tells Hibernate to auto-generate the ID value
    // GenerationType.IDENTITY uses PostgreSQL SERIAL / auto-increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column customizes how this field maps to a database column
    // nullable = false means the database enforces a NOT NULL constraint
    // unique = true means no two products can have the same name
    @Column(nullable = false, unique = true)
    private String name;

    // The product description — optional field, can be null
    private String description;

    // @Column(nullable = false) means price is required
    // Every product must have a price
    @Column(nullable = false)
    private Double price;

    // Stock quantity — how many units are available
    // Defaults to 0 if not provided
    @Column(nullable = false)
    private Integer stock;
}