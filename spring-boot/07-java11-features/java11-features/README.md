# Java 9-11 Features — A Guided Tour

Every language and API addition between Java 8 and Java 11 (the next LTS
release) — `var`, new `String`/`Optional`/`Collection` methods, `Path.of` and
`Files.readString`/`writeString`, and the standardized
`java.net.http.HttpClient` — exposed as runnable REST endpoints. Built with
**Spring Boot 3.5.3 / Java 21**. Picks up exactly where
[`../../06-java8-features`](../15-java8-features) left off.

> 📚 **Docs**
> - **[LEARNING.md](LEARNING.md)** — the full narrative: what each feature
>   is, why it was added, how it works, and common pitfalls. **Read this
>   first — it's the point of the app.**
> - **[API_GUIDE.md](API_GUIDE.md)** — every endpoint with curl examples and
>   what to expect back. *Read this to run it.*
> - **[INTERVIEW_QUESTIONS.md](INTERVIEW_QUESTIONS.md)** — a standalone,
>   question-by-question interview-prep sheet covering `var`'s rules and
>   restrictions, `strip` vs `trim`, `Predicate.not`, and
>   `HttpClient`'s sync vs async request styles.

## Why an API instead of just reading source code

Every demo endpoint returns not just a **result**, but the exact **code
snippet** that produced it and a **plain-English explanation** — see
`dto.Java11DemoResponse`. You can `curl` any endpoint and get a
self-contained lesson back, no source file required, though the source is
just as heavily commented for when you want the full "why".

## What it demonstrates, in reading order

1. **`/java11/var/*`** — local-variable type inference (`var`, Java 10, JEP
   286), its extension to lambda parameters (Java 11, JEP 323, mainly so
   they can carry annotations), and the compile-time restrictions on where
   `var` can and cannot be used.
2. **`/java11/string/*`** — `String.isBlank()`, `strip()`/`stripLeading()`/
   `stripTrailing()` (Unicode-aware, unlike the legacy `trim()`),
   `repeat(int)`, and `lines()`.
3. **`/java11/functional/*`** — `Predicate.not(Predicate)`,
   `Optional.isEmpty()`, and `Collection.toArray(IntFunction)`.
4. **`/java11/nio/*`** — `Path.of(...)` (replacing `Paths.get(...)`) and
   `Files.readString`/`writeString` (one-line whole-file I/O).
5. **`/java11/httpclient/*`** — `java.net.http.HttpClient`, standardized in
   Java 11, with both a blocking synchronous request and a non-blocking
   `CompletableFuture`-based asynchronous one, against a tiny embedded local
   server so the demo never depends on external network access.

## Run

```bash
cd spring-boot/16-java11-features/java11-features
./mvnw spring-boot:run
```

- Swagger UI → http://localhost:8080/api/swagger-ui.html
- Health     → http://localhost:8080/api/actuator/health

Try the single clearest demo first — `strip()` correctly removing Unicode
whitespace that the legacy `trim()` leaves behind:

```bash
curl "http://localhost:8080/api/java11/string/strip-variants"
```

## Run with Docker

```bash
cd spring-boot/16-java11-features/java11-features

# Option A — Docker Compose (recommended)
docker compose up --build

# Option B — plain docker build + run
docker build -t dockyard/java11-features:1.0.0 .
docker run --rm -p 8080:8080 dockyard/java11-features:1.0.0
```

## Verify

```bash
cd spring-boot/16-java11-features/java11-features
./mvnw test
./mvnw spring-boot:run
```