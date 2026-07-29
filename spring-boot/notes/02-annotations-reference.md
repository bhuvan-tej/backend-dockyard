# 02 · Annotations Master Reference

> **Every annotation used across the 5 projects in this repo**, grouped by purpose.
> Format for each: 🧩 what · 🎯 why · 🧪 example · ⚠️ gotcha.
> This is the file to keep open during interviews.

---

## 🚀 1. Bootstrapping

### `@SpringBootApplication`
- 🧩 Marks the main class; bundles `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`.
- 🎯 One annotation to start the whole app.
- 🧪 `@SpringBootApplication public class RestApiCrudApplication { ... }`
- ⚠️ Must sit in the **root package** so component scanning finds everything below it.

---

## 🏷️ 2. Stereotype annotations (register a bean)

These all tell Spring "manage this class as a bean". They're technically the same
under the hood (`@Component`), but the specific name documents the class's **role**.

### `@Component`
- 🧩 Generic "this is a Spring-managed bean".
- 🎯 Use for helper/util classes that don't fit service/repo/controller.
- 🧪 `@Component public class TextChunker { ... }` (in `07-ai-rag-service`)

### `@Service`
- 🧩 A `@Component` that marks the **business-logic** layer.
- 🎯 Semantic clarity; a natural place for `@Transactional`.
- 🧪 `@Service public class ProductService { ... }`

### `@Repository`
- 🧩 A `@Component` for the **data-access** layer.
- 🎯 Adds **exception translation** (converts JPA/SQL exceptions into Spring's
  `DataAccessException` hierarchy).
- 🧪 `@Repository public interface ProductRepository extends JpaRepository<Product, Long> {}`
- ⚠️ On a `JpaRepository` interface it's optional (Spring Data registers it anyway) but
  it's good documentation.

### `@RestController`
- 🧩 `@Controller` + `@ResponseBody` — a web controller whose return values become the
  **HTTP response body** (JSON), not a view name.
- 🎯 Building REST/JSON APIs.
- 🧪 `@RestController public class ProductController { ... }`

### `@Configuration`
- 🧩 A class that defines beans via `@Bean` methods.
- 🎯 Java-based configuration (replaces XML).
- 🧪 `@Configuration public class AiBeans { ... }`

> **Interview line:** "`@Service`, `@Repository`, `@Controller` are specializations of
> `@Component`. Functionally similar, but they communicate intent and enable extra
> behaviour like exception translation for `@Repository`."

---

## 🌐 3. Web / REST mapping

### `@RequestMapping`
- 🧩 Maps HTTP requests to a class or method; base path when on a class.
- 🧪 `@RequestMapping("/api/products")` on the controller → every method is prefixed with it.

### `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping`
- 🧩 Shortcuts for `@RequestMapping(method = GET/POST/PUT/DELETE)`.
- 🎯 Map a method to an HTTP verb + path.
- 🧪
  ```java
  @GetMapping("/{id}")           // GET /api/products/42
  @PostMapping                   // POST /api/products
  @PutMapping("/{id}")           // PUT /api/products/42
  @DeleteMapping("/{id}")        // DELETE /api/products/42
  ```

### `@PathVariable`
- 🧩 Binds a `{placeholder}` in the URL to a method parameter.
- 🧪 `public ProductResponse get(@PathVariable Long id)` for `/api/products/{id}`.

### `@RequestParam`
- 🧩 Binds a **query string** parameter (`?page=0&size=20`).
- 🎯 Optional filters, pagination, search.
- 🧪 `@RequestParam(defaultValue = "0") int page`
- ⚠️ Add `required = false` or a `defaultValue` for optional params, else 400 if missing.

### `@RequestBody`
- 🧩 Deserializes the JSON request body into a Java object (via Jackson).
- 🧪 `public ProductResponse create(@RequestBody @Valid ProductRequest req)`

### `@RequestHeader`
- 🧩 Binds an HTTP header to a parameter.
- 🧪 `@RequestHeader("Authorization") String authHeader` (used in `03-otp-jwt-auth`).

### `@ResponseStatus`
- 🧩 Sets the HTTP status code returned by a method or thrown by an exception.
- 🧪 `@ResponseStatus(HttpStatus.NOT_FOUND)` on `ResourceNotFoundException`.

> See **[03 – REST & Web](./03-rest-api-web.md)** for full request flow and status codes.

---

## ✅ 4. Validation (Jakarta Bean Validation)

### `@Valid`
- 🧩 Tells Spring to **run validation** on the annotated object.
- 🧪 `@RequestBody @Valid ProductRequest req` → constraints below are checked.
- ⚠️ Without `@Valid`, the constraint annotations do **nothing**.

### Constraint annotations (put on DTO fields)

| Annotation | Checks | Example |
|-----------|--------|---------|
| `@NotNull` | not null (but "" is OK) | `@NotNull Long categoryId` |
| `@NotBlank` | not null **and** not empty/whitespace (Strings) | `@NotBlank String name` |
| `@Size` | length/size range | `@Size(min = 2, max = 100)` |
| `@Min` / `@Max` | numeric bounds | `@Min(0) int stock` |
| `@Positive` | number > 0 | `@Positive BigDecimal price` |
| `@Pattern` | regex match | `@Pattern(regexp = "\\d{6}")` (OTP) |
| `@Email` | valid email format | `@Email String email` |

- 🧪
  ```java
  public record ProductRequest(
      @NotBlank(message = "Name is required")
      @Size(min = 2, max = 100) String name,

      @NotNull @Positive BigDecimal price,

      @Min(0) int stock
  ) {}
  ```
- ⚠️ Use `@NotBlank` for Strings, `@NotNull` for objects/numbers, `@NotEmpty` for collections.

> Full details: **[05 – Validation](./05-validation.md)**.

---

## 🗄️ 5. JPA / persistence (entity ↔ table)

### `@Entity`
- 🧩 Marks a class as a JPA entity → maps to a database table.
- 🧪 `@Entity public class Product { ... }`

### `@Table`
- 🧩 Customises table name/indexes.
- 🧪 `@Table(name = "products", indexes = @Index(name = "idx_name", columnList = "name"))`

### `@Id`
- 🧩 Marks the primary key field.

### `@GeneratedValue`
- 🧩 How the PK is generated.
- 🧪 `@GeneratedValue(strategy = GenerationType.IDENTITY)` → DB auto-increment.

### `@Column`
- 🧩 Customises a column (name, nullable, length, unique…).
- 🧪 `@Column(nullable = false, length = 100)`

### `@Enumerated`
- 🧩 Persists a Java enum.
- 🧪 `@Enumerated(EnumType.STRING)` → stores `"ACTIVE"` not `0`.
- ⚠️ **Always use `EnumType.STRING`.** `ORDINAL` breaks if you reorder the enum.

### `@CreationTimestamp` / `@UpdateTimestamp` (Hibernate)
- 🧩 Auto-set timestamps on insert / update.
- 🧪 `@CreationTimestamp private Instant createdAt;`

### `@Index`
- 🧩 Declares a DB index (inside `@Table`) for faster lookups.

### `@Query` (Spring Data)
- 🧩 Write a custom JPQL/SQL query on a repository method.
- 🧪 `@Query("SELECT p FROM Product p WHERE p.stock < :threshold")`

### `@Param`
- 🧩 Binds a method arg to a **named** query parameter (`:threshold`).

### `@Modifying`
- 🧩 Marks a `@Query` as an **UPDATE/DELETE** (not a SELECT).
- ⚠️ Needs `@Transactional` to run.

### `@Transactional`
- 🧩 Wraps the method in a DB transaction — commit on success, **rollback on unchecked exception**.
- 🎯 Atomicity: all-or-nothing.
- 🧪 `@Transactional public ProductResponse create(...) { ... }`
- ⚠️ Only works on **public** methods called from **another bean** (proxy limitation).
  Self-invocation bypasses it.

> Full details: **[04 – Data & JPA](./04-data-jpa.md)**.

---

## 🧯 6. Exception handling

### `@RestControllerAdvice`
- 🧩 `@ControllerAdvice` + `@ResponseBody` — a global handler whose responses are JSON.
- 🎯 Centralise error handling for all controllers.
- 🧪 `@RestControllerAdvice public class GlobalExceptionHandler { ... }`

### `@ExceptionHandler`
- 🧩 Handles a specific exception type inside a controller or advice.
- 🧪
  ```java
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(...) { ... }
  ```

> Full details: **[06 – Exception Handling](./06-exception-handling.md)**.

---

## 🧵 7. Dependency injection & beans

### `@Autowired`
- 🧩 Asks Spring to inject a bean.
- 🧪 Field: `@Autowired private ProductService svc;`
- ⚠️ On a **single constructor**, `@Autowired` is **optional** (Spring infers it). Prefer
  constructor injection + `@RequiredArgsConstructor` instead of field injection.

### `@Bean`
- 🧩 Declares a bean via a factory method inside a `@Configuration` class.
- 🎯 Register objects you **don't own** (library classes you can't annotate).
- 🧪
  ```java
  @Configuration
  public class AiBeans {
      @Bean
      ChatClient chatClient(ChatClient.Builder builder) {
          return builder.build();
      }
  }
  ```

### `@Configuration`
- (See stereotypes above.) Holds `@Bean` methods.

### `@EnableConfigurationProperties`
- 🧩 Activates a `@ConfigurationProperties` class as a bean.
- 🧪 `@EnableConfigurationProperties(RagProperties.class)`

> Full details: **[07 – DI & Beans](./07-dependency-injection-beans.md)**.

---

## ⚙️ 8. Configuration / properties

### `@ConfigurationProperties`
- 🧩 Binds a group of properties (`rag.*`) from `application.yml` to a typed class.
- 🧪 `@ConfigurationProperties(prefix = "rag") public class RagProperties { ... }`
- ⚠️ Needs `@EnableConfigurationProperties` **or** `@Component` to become a bean.

### `@Value`
- 🧩 Injects a single property value.
- 🧪 `@Value("${app.name}") String name;`
- 💡 Prefer `@ConfigurationProperties` for groups of related settings.

> Full details: **[11 – Configuration](./11-configuration-properties.md)**.

---

## 🍬 9. Lombok (compile-time boilerplate generators)

| Annotation | Generates |
|-----------|-----------|
| `@Data` | getters, setters, `toString`, `equals`, `hashCode`, required-args ctor |
| `@Getter` / `@Setter` | just getters / setters |
| `@Builder` | fluent builder: `Product.builder().name("x").build()` |
| `@NoArgsConstructor` | empty constructor (JPA needs this) |
| `@AllArgsConstructor` | constructor with every field |
| `@RequiredArgsConstructor` | constructor for `final` fields → **constructor injection** |
| `@Slf4j` | a `log` field: `log.info("...")` |

- ⚠️ On JPA entities avoid `@Data` (its `equals/hashCode` and `toString` can trigger lazy
  loading / recursion). Prefer `@Getter/@Setter` + explicit `equals` on the id.

> Full details: **[08 – Lombok](./08-lombok.md)**.

---

## 📖 10. OpenAPI / Swagger (springdoc)

### `@Tag`
- 🧩 Groups endpoints in Swagger UI. `@Tag(name = "Products", description = "...")`

### `@Operation`
- 🧩 Documents a single endpoint. `@Operation(summary = "Create a product")`

### `@Parameter`
- 🧩 Documents a parameter. `@Parameter(description = "Product id", example = "42")`

### `@SecurityRequirement`
- 🧩 Marks an endpoint as requiring auth in Swagger. `@SecurityRequirement(name = "bearerAuth")`

> These are **documentation only** — they don't change behaviour, they power the
> Swagger UI at `/swagger-ui.html`.

---

## 🧪 11. Testing

| Annotation | Purpose |
|-----------|---------|
| `@SpringBootTest` | Loads the full application context (integration test) |
| `@WebMvcTest` | Loads only the web layer (controller slice test) |
| `@AutoConfigureMockMvc` | Provides a `MockMvc` to call endpoints without a real server |
| `@Test` | JUnit 5 — marks a test method |
| `@DisplayName` | Human-readable test name |
| `@BeforeEach` | Runs before every test (setup) |
| `@ParameterizedTest` | Runs one test with multiple inputs |
| `@ExtendWith(MockitoExtension.class)` | Enables Mockito in plain JUnit tests |
| `@Mock` | Creates a mock object (Mockito) |
| `@MockBean` / `@MockitoBean` | Puts a mock **into the Spring context** (replaces the real bean) |
| `@Param` (jqwik/params) | Supplies test arguments |

> Full details: **[10 – Testing](./10-testing.md)**.

---

## 🎨 12. JSON serialization (Jackson)

### `@JsonInclude`
- 🧩 Controls which fields appear in JSON.
- 🧪 `@JsonInclude(JsonInclude.Include.NON_NULL)` → omit null fields from the response.

---

## 📎 13. Javadoc tags (not Spring, but seen in the code)

These appear inside `/** ... */` comments, not on code:

| Tag | Meaning |
|-----|---------|
| `@param` | documents a method parameter |
| `@return` | documents the return value |
| `@throws` | documents a thrown exception |
| `{@code ...}` | inline monospaced code |
| `{@link ...}` | link to another class/method |

⚠️ Don't confuse Javadoc `@param` with Spring's `@RequestParam` — different worlds.

---

### 🧾 One-glance summary table

| Category | Annotations |
|----------|-------------|
| Bootstrap | `@SpringBootApplication` |
| Stereotypes | `@Component` `@Service` `@Repository` `@RestController` `@Configuration` |
| Web | `@RequestMapping` `@GetMapping` `@PostMapping` `@PutMapping` `@DeleteMapping` `@PathVariable` `@RequestParam` `@RequestBody` `@RequestHeader` `@ResponseStatus` |
| Validation | `@Valid` `@NotNull` `@NotBlank` `@Size` `@Min` `@Max` `@Positive` `@Pattern` `@Email` |
| JPA | `@Entity` `@Table` `@Id` `@GeneratedValue` `@Column` `@Enumerated` `@Index` `@CreationTimestamp` `@UpdateTimestamp` `@Query` `@Param` `@Modifying` `@Transactional` |
| Errors | `@RestControllerAdvice` `@ExceptionHandler` |
| DI/Beans | `@Autowired` `@Bean` `@EnableConfigurationProperties` |
| Config | `@ConfigurationProperties` `@Value` |
| Lombok | `@Data` `@Getter` `@Setter` `@Builder` `@Slf4j` `@RequiredArgsConstructor` `@NoArgsConstructor` `@AllArgsConstructor` |
| OpenAPI | `@Tag` `@Operation` `@Parameter` `@SecurityRequirement` |
| Testing | `@SpringBootTest` `@WebMvcTest` `@AutoConfigureMockMvc` `@Test` `@Mock` `@MockBean` `@MockitoBean` `@DisplayName` `@BeforeEach` |
| JSON | `@JsonInclude` |

➡️ Next: **[03 – REST & Web Layer](./03-rest-api-web.md)**

