# Java Streams — A Guided Tour

Every corner of the `java.util.stream` API demonstrated against one
shared 20-employee dataset, via a REST API. Built with **Spring Boot 3.5.3 /
Java 21**.

> 📚 **Docs**
> - **[LEARNING.md](LEARNING.md)** — the full narrative: what a stream actually
>   is, the lazy pipeline model, every operation explained in detail, common
>   pitfalls, and interview quick-hits. **Read this first — it's the point of
>   the app.**
> - **[API_GUIDE.md](API_GUIDE.md)** — every endpoint with curl examples.
>   *Read this to run it.*

## Why an API instead of just reading source code

Every demo endpoint returns not just a **result**, but the exact **code
snippet** that produced it and a **plain-English explanation** — see
`dto.StreamDemoResponse`. You can `curl` any endpoint and get a
self-contained lesson back, no source file required, though the source is
just as heavily commented for when you want the full "why".

## What it demonstrates, in reading order

1. **`/streams/creation/*`** — every way to OBTAIN a stream: from a
   collection, `Stream.of(...)`, an array, `IntStream.range`,
   `Stream.generate`/`Stream.iterate` (infinite, must be bounded),
   `Stream.empty()`, `Stream.concat`.
2. **`/streams/intermediate/*`** — `filter`, `map`, `flatMap`, `distinct`,
   `sorted` (natural + `Comparator`), `peek`, `limit`, `skip`. All **lazy** —
   nothing runs until a terminal operation is attached.
3. **`/streams/terminal/*`** — `forEach`, `count`, `min`/`max`, `anyMatch`/
   `allMatch`/`noneMatch`, `findFirst`/`findAny`, and all three `reduce`
   overloads. These are what actually **execute** the pipeline; some
   **short-circuit**, some don't.
4. **`/streams/collectors/*`** — `toList`, `toSet`, `toMap` (+ the merge-
   function overload), `joining`, `groupingBy` (+ downstream collectors +
   nested grouping), `partitioningBy`, `summarizingDouble`, `averagingInt`,
   `summingDouble`, `toCollection`, `minBy`/`maxBy`, `reducing`.
5. **`/streams/primitives/*`** — `IntStream`/`DoubleStream` via
   `mapToInt`/`mapToDouble`, and the terminal operations only a PRIMITIVE
   stream has: `sum`, `average`, `max`/`min` (as `OptionalInt`/`OptionalDouble`),
   `summaryStatistics`, plus `.boxed()` to convert back to an object Stream.
6. **`/streams/parallel/*`** — `stream()` vs `parallelStream()` on **CPU-bound**
   work (the opposite scenario from `04-virtual-threads`, which helps
   I/O-bound work), plus the classic shared-mutable-state data-race pitfall.
7. **`/streams/modern/*`** — everything the Stream/Collectors API gained
   **after** Java 8 (Java 9–16): `Stream.toList()`, `takeWhile`/`dropWhile`,
   `Collectors.teeing`, `Stream.ofNullable`/`Optional.stream()`,
   `Stream.mapMulti`, `Collectors.filtering`, and the 3-arg `Stream.iterate`
   overload — each paired against the Java 8-era method it replaces or
   complements, so you can see exactly what changed and why the rest of
   this app avoids them. See LEARNING.md section 10 for the full "why leave
   these out elsewhere" explanation.

## Run

```bash
cd spring-boot/05-java-streams/java-streams
./mvnw spring-boot:run
```

- Swagger UI → http://localhost:8080/api/swagger-ui.html
- Health     → http://localhost:8080/api/actuator/health

Try the collectors first — they're the most immediately useful part of the API:

```bash
curl "http://localhost:8080/api/streams/collectors/groupingby-counting"
```

## Run with Docker

```bash
cd spring-boot/05-java-streams/java-streams

# Option A — Docker Compose (recommended)
docker compose up --build

# Option B — plain docker build + run
docker build -t dockyard/java-streams:1.0.0 .
docker run --rm -p 8080:8080 dockyard/java-streams:1.0.0
```

## Verify

```bash
cd spring-boot/05-java-streams/java-streams
./mvnw test
./mvnw spring-boot:run
```