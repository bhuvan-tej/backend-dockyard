# 07 · Dependency Injection & Beans

> How Spring creates objects and wires them together.

---

## 🫘 What is a "bean"?

A **bean** is any object the Spring container creates, configures, and manages.
Two ways to make one:

1. **Annotate the class** with a stereotype (`@Component`, `@Service`, `@Repository`,
   `@RestController`, `@Configuration`) → Spring finds it via component scanning.
2. **Declare it with `@Bean`** inside a `@Configuration` class → for objects you don't own.

---

## 🏷️ Stereotypes = auto-registered beans

```java
@Service         public class ProductService { ... }   // business logic
@Repository      public interface ProductRepository ... // data access
@RestController  public class ProductController { ... } // web
@Component       public class TextChunker { ... }       // generic helper
@Configuration   public class AiBeans { ... }           // bean definitions
```

All are specializations of `@Component`. Component scanning (enabled by
`@SpringBootApplication`) discovers them in the main package and below.

---

## 💉 Injecting dependencies — prefer the constructor

```java
@Service
@RequiredArgsConstructor              // Lombok → constructor for all final fields
public class ProductService {
    private final ProductRepository repository;   // injected by Spring
    private final NotificationClient notifier;    // injected too
}
```

Equivalent hand-written version:
```java
@Service
public class ProductService {
    private final ProductRepository repository;
    public ProductService(ProductRepository repository) {   // Spring injects here
        this.repository = repository;
    }
}
```

### Why constructor injection wins

| Benefit | Explanation |
|---------|-------------|
| **Immutability** | fields can be `final` |
| **Testability** | just `new ProductService(mockRepo)` in tests — no Spring needed |
| **Fail fast** | missing dependency = startup error, not NPE at runtime |
| **No reflection hacks** | works without `@Autowired` on a single constructor |

### `@Autowired`
- Explicitly requests injection. On a **single constructor it's optional**.
- Field injection (`@Autowired private X x;`) works but is discouraged (hard to test,
  hides dependencies, can't be `final`).

---

## 🏭 `@Bean` — register objects you don't own

You can't put `@Component` on a library class (e.g. Spring AI's `ChatClient`), so you
build it in a `@Configuration`:

```java
@Configuration
public class AiBeans {

    @Bean                                    // method name = bean name ("chatClient")
    public ChatClient chatClient(ChatClient.Builder builder) {  // params are injected
        return builder
                .defaultSystem("You are a helpful assistant.")
                .build();
    }
}
```

- Runs **once** at startup; the return value becomes a singleton bean.
- Method **parameters are themselves injected** by Spring.

### `@Component`/`@Service` vs `@Bean`

| | `@Component` (& friends) | `@Bean` |
|---|--------------------------|---------|
| Applied to | **your** classes | a **method** in `@Configuration` |
| Discovery | component scan | explicit method call |
| Use when | you own the class | you configure a 3rd-party class |

---

## 🔁 Bean scope & lifecycle

- Default scope is **singleton** — one shared instance for the whole app.
- Other scopes: `prototype` (new each time), `request`, `session` (web).
- Lifecycle hooks: `@PostConstruct` (after injection), `@PreDestroy` (before shutdown).

```java
@Component
public class Cache {
    @PostConstruct void warmUp() { /* runs once after construction */ }
}
```

---

## 🤝 Resolving ambiguity (multiple candidates)

If two beans match one type, Spring can't choose. Fix with:

- `@Primary` — mark one as the default.
- `@Qualifier("name")` — pick a specific bean by name.

```java
@Bean @Primary ChatClient fastClient(...) { ... }
@Bean @Qualifier("accurate") ChatClient accurateClient(...) { ... }

public Engine(@Qualifier("accurate") ChatClient client) { ... }
```

---

## ⚙️ Activating configuration properties as beans

```java
@Configuration
@EnableConfigurationProperties(RagProperties.class)  // makes RagProperties a bean
public class RagConfig { ... }
```
See **[11 – Configuration](./11-configuration-properties.md)**.

➡️ Next: **[08 – Lombok](./08-lombok.md)**

