# Java 12-17 Features — A Guided Tour

Every language and API addition between Java 11 and Java 17 (the next LTS
release) — **records**, **sealed classes**, **pattern matching for
`instanceof`**, **switch expressions**, **text blocks**, `Stream.toList()` /
`Collectors.teeing`, helpful `NullPointerException`s and the new
`RandomGenerator` API — exposed as runnable REST endpoints. Built with
**Spring Boot 3.5.3 / Java 21**. Picks up exactly where
[`../../07-java11-features`](../../07-java11-features) left off.

> 📚 **Docs**
> - **[LEARNING.md](LEARNING.md)** — the full narrative: what each feature
>   is, why it was added, how it works, and common pitfalls. **Read this
>   first — it's the point of the app.**
> - **[API_GUIDE.md](API_GUIDE.md)** — every endpoint with curl examples and
>   what to expect back. *Read this to run it.*
> - **[INTERVIEW_QUESTIONS.md](INTERVIEW_QUESTIONS.md)** — a standalone,
>   33-question interview-prep sheet covering compact constructors, shallow
>   immutability, the `final`/`sealed`/`non-sealed` rule, flow scoping,
>   `yield` vs `return`, incidental whitespace, and
>   `Stream.toList()` vs `Collectors.toList()` vs `toUnmodifiableList()`.

## Why an API instead of just reading source code

Every demo endpoint returns not just a **result**, but the exact **code
snippet** that produced it and a **plain-English explanation** — see
`dto.Java17DemoResponse`. You can `curl` any endpoint and get a
self-contained lesson back, no source file required, though the source is
just as heavily commented for when you want the full "why".

Several endpoints go further and *prove* their claim at runtime by
reflection rather than asserting it — `Class.getRecordComponents()` for what
a record generates, `Class.getPermittedSubclasses()` for a sealed
hierarchy's closed set, and a caught `UnsupportedOperationException` for
`Stream.toList()` being unmodifiable.

## What it demonstrates, in reading order

1. **`/java17/records/*`** — records (Java 16, JEP 395): what the compiler
   generates, the compact constructor as the home for invariants, the
   shallow-immutability trap, local records, and the hard limitations.
2. **`/java17/sealed/*`** — sealed classes and interfaces (Java 17, JEP
   409): closed hierarchies, the `final`/`sealed`/`non-sealed` rule, and
   exhaustive `switch` with no `default` branch.
3. **`/java17/pattern-matching/*`** — pattern matching for `instanceof`
   (Java 16, JEP 394) and switch expressions (Java 14, JEP 361): flow
   scoping, arrow labels, `yield`, and sealed-type dispatch.
4. **`/java17/text-blocks/*`** — text blocks (Java 15, JEP 378): incidental
   vs essential whitespace, the `\` and `\s` escapes, and
   `String.formatted(...)` as the stand-in for string interpolation.
5. **`/java17/api/*`** — `Stream.toList()` (16), `Collectors.teeing()` (12),
   and `String.formatted`/`indent`/`transform` (12/15).
6. **`/java17/jdk/*`** — helpful `NullPointerException` messages (14, JEP
   358), the pluggable `RandomGenerator` API (17, JEP 356), and compact
   number formatting (12).

> **Version note.** Sealing is final in Java 17, but the *type patterns in a
> `switch`* it was designed for (`case Circle c ->`) were **preview** in 17
> (JEP 406) and only became standard in **Java 21** (JEP 441). This repo
> compiles with Java 21, so the examples run as written — and the docs and
> code comments call this out wherever it appears, because it's a favourite
> interview follow-up.

## Run

```bash
cd spring-boot/17-java17-features/java17-features
./mvnw spring-boot:run
```

- Swagger UI → http://localhost:8080/api/swagger-ui.html
- Health     → http://localhost:8080/api/actuator/health

Try the single clearest demo first — the JVM naming the exact expression
that was null, instead of the bare pre-Java-14 stack trace:

```bash
curl "http://localhost:8080/api/java17/jdk/helpful-npe"
```

## Run with Docker

```bash
cd spring-boot/17-java17-features/java17-features

# Option A — Docker Compose (recommended)
docker compose up --build

# Option B — plain docker build + run
docker build -t dockyard/java17-features:1.0.0 .
docker run --rm -p 8080:8080 dockyard/java17-features:1.0.0
```

## Verify

```bash
cd spring-boot/17-java17-features/java17-features
./mvnw test
./mvnw spring-boot:run
```