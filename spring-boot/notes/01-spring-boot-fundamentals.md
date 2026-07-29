# 01 · Spring Boot Fundamentals

> The mental model you need before touching any annotation.

---

## 🌰 What is Spring Boot in one sentence?

Spring Boot is **Spring Framework + sensible defaults + an embedded server**, so you
can run a production-grade app with `main()` and almost no XML/config.

- **Spring Framework** gives you the *Inversion of Control (IoC) container* and
  *Dependency Injection (DI)*.
- **Spring Boot** adds **auto-configuration**, **starter dependencies**, and an
  **embedded Tomcat** so there's nothing to deploy to.

---

## 🔄 Inversion of Control (IoC) & Dependency Injection (DI)

**Normally**, your object creates the things it depends on:

```java
class ProductService {
    private final ProductRepository repo = new ProductRepository(); // tight coupling ❌
}
```

**With Spring**, you *declare* what you need and the **container** creates and injects it:

```java
@Service
@RequiredArgsConstructor            // Lombok generates the constructor
public class ProductService {
    private final ProductRepository repo;   // Spring injects this ✅
}
```

- **IoC** = you don't control object creation; the **container** does.
- **DI** = the container *injects* dependencies into your object.
- The container is called the **ApplicationContext**. It holds **beans** (Spring-managed objects).

### 3 ways to inject (constructor is best)

| Type | Example | Verdict |
|------|---------|---------|
| **Constructor** | `public Foo(Bar bar)` | ✅ Preferred — immutable, testable, fails fast |
| **Field** | `@Autowired private Bar bar;` | ⚠️ Hard to test, hidden dependencies |
| **Setter** | `@Autowired public void setBar(...)` | For optional dependencies only |

> 💡 In this repo you'll see `@RequiredArgsConstructor` (Lombok) everywhere — it generates
> a constructor for all `final` fields, giving you constructor injection **without boilerplate**.

---

## ⚙️ Auto-configuration

When you add a starter (e.g. `spring-boot-starter-data-jpa`), Spring Boot **looks at the
classpath** and configures beans automatically:

- Sees H2/PostgreSQL driver → configures a `DataSource`.
- Sees Spring Web → starts embedded Tomcat, sets up Jackson JSON.
- Sees Spring Security → locks down all endpoints by default.

This is driven by `@SpringBootApplication` (which includes `@EnableAutoConfiguration`).

---

## 🚀 The application entry point

```java
@SpringBootApplication          // ← the "magic" annotation
public class RestApiCrudApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestApiCrudApplication.class, args);
    }
}
```

### `@SpringBootApplication` = 3 annotations in one

| Bundled annotation | What it does |
|--------------------|--------------|
| `@Configuration` | Marks the class as a source of bean definitions |
| `@EnableAutoConfiguration` | Turns on Spring Boot's auto-config magic |
| `@ComponentScan` | Scans this package **and sub-packages** for `@Component`, `@Service`, `@Repository`, `@Controller` |

⚠️ **Gotcha:** `@ComponentScan` only scans **below** the main class's package.
That's why the main class lives at the **root package** (`com.dockyard.restapicrud`).
If you put a `@Service` outside that package, Spring won't find it.

---

## 🧱 The layered architecture (used in every project here)

```
controller/   → HTTP layer      (@RestController)   talks to → service
service/      → business logic  (@Service)          talks to → repository
repository/   → data access     (@Repository)       talks to → database
entity/       → DB table mapping (@Entity)
dto/          → data shapes crossing the API boundary
config/       → beans & setup    (@Configuration)
exception/    → error handling   (@RestControllerAdvice)
```

**Why separate layers?**
- **Single Responsibility** — each layer has one job.
- **Testability** — mock the layer below.
- **DTOs vs Entities** — never expose your database shape directly to clients.

---

## 🔁 Application lifecycle (simplified)

```
main() → SpringApplication.run()
   │
   ├─ create ApplicationContext (the IoC container)
   ├─ component scan → find all @Component/@Service/etc.
   ├─ run auto-configuration
   ├─ instantiate beans + inject dependencies
   ├─ start embedded Tomcat on port 8080
   └─ app is ready to serve requests
```

---

## 🧠 Key terms cheat sheet

| Term | Meaning |
|------|---------|
| **Bean** | Any object managed by the Spring container |
| **ApplicationContext** | The IoC container that holds all beans |
| **Component scanning** | Auto-discovery of `@Component`-annotated classes |
| **Starter** | A curated set of dependencies (`spring-boot-starter-web`, etc.) |
| **Auto-configuration** | Beans configured for you based on the classpath |
| **Embedded server** | Tomcat/Jetty runs *inside* the JAR — no external server |

➡️ Next: **[02 – Annotations Master Reference](./02-annotations-reference.md)**

