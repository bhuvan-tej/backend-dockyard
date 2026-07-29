# 13 · Interview Quick Sheet

> Rapid-fire revision. Cover the answer, recall it, then check.

---

## 🌱 Core Spring Boot

**Q: What does `@SpringBootApplication` do?**
Bundles `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`. Bootstraps the app,
enables auto-config, and scans the current package and below for components.

**Q: IoC vs DI?**
IoC = the container controls object creation (not you). DI = the container injects
dependencies into your objects. DI is how IoC is achieved.

**Q: Constructor vs field injection — which and why?**
Constructor. Enables `final` (immutable), easy to unit-test (`new Service(mock)`), and
fails fast at startup if a dependency is missing.

**Q: What is a bean?**
Any object managed by the Spring container (created, wired, lifecycle-managed).

**Q: `@Component` vs `@Service` vs `@Repository` vs `@Controller`?**
All are stereotypes → all register beans. Names document intent. `@Repository` adds
persistence exception translation; `@RestController` = `@Controller` + `@ResponseBody`.

**Q: `@Bean` vs `@Component`?**
`@Component` on **your** classes (found by scanning). `@Bean` on a **method** in a
`@Configuration` to register objects you don't own (library types).

---

## 🌐 Web

**Q: `@RequestParam` vs `@PathVariable`?**
`@PathVariable` binds a `{segment}` in the URL path. `@RequestParam` binds a `?query=value`.

**Q: `@RequestBody`?**
Deserializes the JSON request body into a Java object via Jackson.

**Q: `@RestController` vs `@Controller`?**
`@RestController` returns data (JSON) as the response body. `@Controller` returns view names.

**Q: Status codes for CRUD?**
GET 200, POST 201, PUT 200, DELETE 204, bad input 400, not found 404, conflict 409.

**Q: Why DTOs instead of exposing entities?**
Security (hide internal fields), stability (DB changes don't break the API), flexibility
(computed/renamed fields).

---

## 🗄️ Data / JPA

**Q: JPA vs Hibernate vs Spring Data JPA?**
JPA = spec; Hibernate = implementation that writes SQL; Spring Data JPA = repository
abstraction that auto-implements queries.

**Q: What does `JpaRepository` give you?**
`save`, `findById`, `findAll`, `deleteById`, `count`, pagination & sorting — for free.

**Q: `@Transactional` — when does it roll back?**
On unchecked (`RuntimeException`) by default; not on checked exceptions unless
`rollbackFor` is set. Works via a proxy → public methods called from another bean only.

**Q: `@GeneratedValue` strategy for Postgres?**
`IDENTITY` (auto-increment) is simplest; `SEQUENCE` batches better.

**Q: Why `@Enumerated(EnumType.STRING)`?**
`ORDINAL` stores the index, which breaks if you reorder the enum. `STRING` is safe.

**Q: The N+1 problem?**
One query for parents + one per child = N+1. Fix with a fetch join / entity graph.

---

## ✅ Validation

**Q: `@NotNull` vs `@NotEmpty` vs `@NotBlank`?**
`@NotNull`: not null. `@NotEmpty`: not null + size>0. `@NotBlank`: not null + has
non-whitespace text (Strings).

**Q: What triggers validation?**
`@Valid` on the parameter. Without it, constraint annotations do nothing.

**Q: What happens on failure?**
Spring throws `MethodArgumentNotValidException` → 400; handle it in the advice for a clean body.

---

## 🧯 Exceptions

**Q: `@RestControllerAdvice` + `@ExceptionHandler`?**
`@RestControllerAdvice` = global JSON exception handler across all controllers.
`@ExceptionHandler(X.class)` maps a specific exception to a response.

**Q: Best practice for error responses?**
Consistent shape, correct status codes, log server-side, never expose stack traces, always
have a catch-all `Exception` handler.

---

## 🍬 Lombok

**Q: `@RequiredArgsConstructor`?**
Generates a constructor for all `final` fields → constructor injection without boilerplate.

**Q: Why avoid `@Data` on entities?**
Its `toString`/`equals`/`hashCode` traverse all fields → lazy-loading issues, recursion,
broken JPA identity. Use `@Getter/@Setter` + id-based equals.

---

## 🔐 Security / JWT

**Q: Access vs refresh token?**
Access: short-lived, stateless, sent on every call. Refresh: long-lived, stored (revocable),
used only to mint new access tokens; rotated on each use.

**Q: Why disable CSRF here?**
CSRF protects cookie-based sessions. A stateless Bearer-token API doesn't use cookies, so
it's unnecessary.

**Q: Is a JWT encrypted?**
No — it's **signed**. Anyone can read the payload; only the holder of the secret can forge
a valid signature. Don't put secrets in it.

**Q: How are OTPs kept safe?**
Hashed at rest, time-limited (expiry), attempt-capped (anti-brute-force), one-time use.

---

## 🧪 Testing

**Q: `@Mock` vs `@MockBean`/`@MockitoBean`?**
`@Mock` = plain Mockito mock (no Spring). `@MockBean`/`@MockitoBean` = mock placed into the
Spring context, replacing the real bean (for slice/integration tests).

**Q: `@WebMvcTest` vs `@SpringBootTest`?**
`@WebMvcTest` loads only the web slice (fast, mock the services). `@SpringBootTest` loads the
full context (slow, end-to-end).

---

## ⚙️ Configuration

**Q: `@Value` vs `@ConfigurationProperties`?**
`@Value` for a single property. `@ConfigurationProperties` for a typed group — supports
relaxed binding and validation. Prefer it for feature config.

**Q: How to keep the same JAR across environments?**
Externalise config in `application.yml`, use profiles (`application-prod.yml`), and inject
secrets via environment variables.

---

## 🤖 Spring AI

**Q: What is RAG?**
Retrieval-Augmented Generation: embed & store your docs, retrieve the most similar chunks
for a question, and put them in the prompt so the LLM answers grounded in your data.

**Q: RAG vs fine-tuning?**
RAG is cheaper, fresher (just update the store), traceable (cite sources), and keeps data
private — no retraining needed.

---

### 🏁 30-second architecture pitch (say this out loud)

> "Requests hit a `@RestController`, which validates DTOs with `@Valid` and delegates to a
> `@Service`. The service holds business logic, runs inside `@Transactional`, and talks to a
> Spring Data `@Repository` mapping JPA `@Entity` classes to tables. Cross-cutting concerns —
> errors via `@RestControllerAdvice`, config via `@ConfigurationProperties`, security via a
> JWT filter chain — keep controllers thin. Lombok removes boilerplate; tests range from
> Mockito unit tests to `@SpringBootTest` integration tests."

⬅️ Back to **[Notes Index](./README.md)**

