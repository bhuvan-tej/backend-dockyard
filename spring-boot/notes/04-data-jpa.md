# 04 · Data & JPA

> Turning Java objects into database rows with Spring Data JPA + Hibernate.

---

## 🧭 The players

| Layer | Tool | Role |
|-------|------|------|
| **JPA** | Jakarta Persistence API | The *specification* (annotations like `@Entity`) |
| **Hibernate** | ORM | The *implementation* that generates SQL |
| **Spring Data JPA** | Spring | Repository abstraction (auto-implements queries) |

You write **entities + repository interfaces**; Spring Data writes the SQL.

---

## 🧱 Mapping an entity

```java
@Entity                                    // this class = a table
@Table(name = "products",                  // table name + indexes
       indexes = @Index(name = "idx_product_name", columnList = "name"))
@Getter @Setter                            // Lombok (avoid @Data on entities)
@NoArgsConstructor                         // JPA requires a no-arg constructor
@AllArgsConstructor @Builder
public class Product {

    @Id                                    // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB auto-increment
    private Long id;

    @Column(nullable = false, length = 100) // NOT NULL, VARCHAR(100)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    private int stock;

    @Enumerated(EnumType.STRING)           // store enum as text ("ACTIVE")
    private Status status;

    @CreationTimestamp                     // set automatically on insert
    private Instant createdAt;

    @UpdateTimestamp                       // updated automatically on save
    private Instant updatedAt;
}
```

### Annotation recap

| Annotation | Meaning |
|-----------|---------|
| `@Entity` | Class maps to a table |
| `@Table` | Custom table name / indexes / constraints |
| `@Id` | Primary key field |
| `@GeneratedValue` | PK generation strategy (`IDENTITY`, `SEQUENCE`, `AUTO`, `UUID`) |
| `@Column` | Column tuning (name, nullable, length, unique) |
| `@Enumerated(STRING)` | Persist enums as text — **always prefer STRING** |
| `@CreationTimestamp` / `@UpdateTimestamp` | Auto timestamps (Hibernate) |
| `@Index` | Declares an index for faster reads |

⚠️ **`@GeneratedValue` strategies:**
- `IDENTITY` — DB auto-increment (Postgres/MySQL). Simple, common.
- `SEQUENCE` — DB sequence; better batching in Postgres.
- `AUTO` — Hibernate picks; can surprise you. Be explicit.

---

## 📚 Repositories

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 1. Derived query — Spring parses the METHOD NAME into SQL
    List<Product> findByNameContainingIgnoreCase(String term);
    Page<Product> findByStatus(Status status, Pageable pageable);

    // 2. Custom JPQL query
    @Query("SELECT p FROM Product p WHERE p.stock < :threshold")
    List<Product> findLowStock(@Param("threshold") int threshold);

    // 3. Modifying query (UPDATE/DELETE) — needs @Modifying + @Transactional
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.stock = 0 WHERE p.id = :id")
    void zeroStock(@Param("id") Long id);
}
```

`JpaRepository<Product, Long>` gives you `save`, `findById`, `findAll`, `deleteById`,
`count`, pagination, sorting — **for free**.

### Derived query keywords
`findBy`, `And`, `Or`, `Containing`, `IgnoreCase`, `Between`, `LessThan`, `OrderBy`,
`In`, `IsNull`, `True/False`… e.g. `findByPriceBetweenAndStatusOrderByPriceAsc(...)`.

### `@Query`, `@Param`, `@Modifying`
- `@Query` — write JPQL (or native SQL with `nativeQuery = true`).
- `@Param` — bind `:named` parameters.
- `@Modifying` — required for `UPDATE`/`DELETE` queries; pair with `@Transactional`.

---

## 🔒 `@Transactional` — the atomic unit

```java
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repo;

    @Transactional                       // commit on success, rollback on exception
    public ProductResponse create(ProductRequest req) {
        Product saved = repo.save(map(req));
        // if anything below throws a RuntimeException → the save is rolled back
        return toResponse(saved);
    }

    @Transactional(readOnly = true)      // optimisation hint for read-only work
    public ProductResponse get(Long id) { ... }
}
```

**Rules to remember:**
- Rolls back on **unchecked** (`RuntimeException`) by default, **not** on checked exceptions
  (override with `rollbackFor = Exception.class`).
- Works via a **proxy** → only on `public` methods called from **another bean**.
  Calling a `@Transactional` method from within the same class **skips** the transaction.
- `readOnly = true` lets Hibernate skip dirty-checking → faster reads.

---

## 🗺️ DTO ↔ Entity mapping

Keep the mapping in the **service** (or a dedicated mapper):

```java
private Product map(ProductRequest r) {
    return Product.builder()
            .name(r.name())
            .price(r.price())
            .stock(r.stock())
            .status(Status.ACTIVE)
            .build();
}

private ProductResponse toResponse(Product p) {
    return new ProductResponse(p.getId(), p.getName(), p.getPrice(), p.getStock() > 0);
}
```

---

## ⚡ N+1 problem (classic interview trap)

Loading a list of parents then a separate query per child = **N+1 queries**.
Fix with a **fetch join** (`@Query("... JOIN FETCH ...")`) or an entity graph.

➡️ Next: **[05 – Validation](./05-validation.md)**

