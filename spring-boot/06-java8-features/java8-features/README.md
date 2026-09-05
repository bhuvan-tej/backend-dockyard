# Core Java 8 Features — A Guided Tour

Every language-level feature Java 8 introduced — lambda expressions,
functional interfaces, method references, default/static interface methods,
`Optional`, and the new `java.time` API — exposed as runnable REST endpoints.
Built with **Spring Boot 3.5.3 / Java 21**.

> 📚 **Docs**
> - **[LEARNING.md](LEARNING.md)** — the full narrative: what each feature
>   is, why it was added, how it works under the hood, and common pitfalls.
>   **Read this first — it's the point of the app.**
> - **[API_GUIDE.md](API_GUIDE.md)** — every endpoint with curl examples and
>   what to expect back. *Read this to run it.*
> - **[INTERVIEW_QUESTIONS.md](INTERVIEW_QUESTIONS.md)** — a standalone,
>   question-by-question interview-prep sheet covering lambdas vs anonymous
>   classes, the four method reference kinds, default/static methods and the
>   diamond problem, `Optional` gotchas, and `Period` vs `Duration`.

## Why an API instead of just reading source code

Every demo endpoint returns not just a **result**, but the exact **code
snippet** that produced it and a **plain-English explanation** — see
`dto.Java8DemoResponse`. You can `curl` any endpoint and get a self-contained
lesson back, no source file required, though the source is just as heavily
commented for when you want the full "why".

## What it demonstrates, in reading order

1. **`/java8/lambda/*`** — lambda syntax forms (no-arg, single-param,
   expression body vs block body) and the "effectively final" rule for
   variable capture.
2. **`/java8/functional-interfaces/*`** — `Predicate`, `Function`,
   `Supplier`, `Consumer`, `BiFunction` from `java.util.function`, composed
   with `and`/`or`/`negate`/`andThen`/`compose`, plus a hand-rolled custom
   `@FunctionalInterface`.
3. **`/java8/method-references/*`** — the four kinds: static
   (`ClassName::staticMethod`), bound instance (`particularObject::method`),
   unbound instance (`ClassName::instanceMethod`), and constructor
   (`ClassName::new`).
4. **`/java8/interface-methods/*`** — `default` and `static` methods on
   interfaces, and the "diamond problem" (two interfaces, same default
   method, resolved via `InterfaceName.super.method()`).
5. **`/java8/optional/*`** — `Optional.of`/`ofNullable`/`empty`, `.map()`/
   `.filter()` pipelines, and the classic `orElse` vs `orElseGet`
   eager-vs-lazy gotcha.
6. **`/java8/datetime/*`** — `LocalDate`/`LocalTime`/`LocalDateTime`,
   `Period` vs `Duration`, `DateTimeFormatter`, and `ZonedDateTime`/`Instant`
   with legacy `java.util.Date` interop.

> The Stream API — Java 8's other headline feature — has its own dedicated,
> deep-dive app: see [`spring-boot/05-java-streams`](../05-java-streams). It
> isn't duplicated here.

## Run

```bash
cd spring-boot/15-java8-features/java8-features
./mvnw spring-boot:run
```

- Swagger UI → http://localhost:8080/api/swagger-ui.html
- Health     → http://localhost:8080/api/actuator/health

Try the single clearest demo first — the `orElse` vs `orElseGet` eager-vs-lazy
gotcha:

```bash
curl "http://localhost:8080/api/java8/optional/or-else-vs-or-else-get"
```

## Run with Docker

```bash
cd spring-boot/15-java8-features/java8-features

# Option A — Docker Compose (recommended)
docker compose up --build

# Option B — plain docker build + run
docker build -t dockyard/java8-features:1.0.0 .
docker run --rm -p 8080:8080 dockyard/java8-features:1.0.0
```

## Verify

```bash
cd spring-boot/15-java8-features/java8-features
./mvnw test
./mvnw spring-boot:run
```