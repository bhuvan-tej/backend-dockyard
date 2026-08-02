# Virtual Threads

Java 21 **virtual threads** with Spring Boot: platform threads vs virtual
threads, measured side by side, with the same simulated blocking workload run
through both. Built with **Spring Boot 3.5.3 / Java 21**, no database, no
external services.

> 📚 **Docs**
> - **[LEARNING.md](LEARNING.md)** — the concepts: platform vs virtual threads,
>   carrier threads, unmounting, why this only helps I/O-bound work.
>   *Read this to understand it.*
> - **[API_GUIDE.md](API_GUIDE.md)** — every endpoint with curl examples and
>   expected timings. *Read this to run it.*

## What it demonstrates

- **The one-line server switch** — `spring.threads.virtual.enabled: true` in
  `application.yml` makes the embedded Tomcat hand every HTTP request to a
  fresh virtual thread instead of borrowing one from a small, fixed pool.
  See it directly: `GET /demo/request-thread`.
- **The manual executor version of the same idea** — `ThreadingConfig` defines
  two `ExecutorService` beans: `Executors.newVirtualThreadPerTaskExecutor()`
  vs a deliberately small `Executors.newFixedThreadPool(20)`. `/demo/run` and
  `/demo/compare` submit identical simulated blocking work to each and measure
  wall-clock time — the difference is not subtle.
- **Why it only helps I/O-bound work** — `DownstreamSimulator` uses a plain
  `Thread.sleep(delayMs)` to stand in for "a slow HTTP call / DB query /
  anything blocking". A virtual thread *unmounts* from its carrier platform
  thread during that sleep, freeing the carrier for other work; a platform
  thread just blocks, full stop. Same line of code, very different cost.
- **Structured, readable code, no reactive types** — every endpoint here reads
  like plain, boring, blocking Java. That's the whole point of virtual
  threads: you get async-style scalability without rewriting your code in a
  reactive style.

## Run

```bash
cd spring-boot/11-virtual-threads/virtual-threads
./mvnw spring-boot:run
```

- Swagger UI → http://localhost:8080/api/swagger-ui.html
- Health     → http://localhost:8080/api/actuator/health
- Thread dump → http://localhost:8080/api/actuator/threaddump (look for `"VirtualThread"` entries)

Try the single clearest demo first:

```bash
curl "http://localhost:8080/api/demo/compare?tasks=100&delayMs=200"
```

With a 20-thread platform pool and 100 tasks that each take 200ms, expect the
platform run to take roughly `(100/20) * 200ms ≈ 1000ms`, while the virtual
run finishes in roughly `200ms` — see **[API_GUIDE.md](API_GUIDE.md)** for the
full walkthrough, including hitting `/demo/request-thread` concurrently.

## Run with Docker

```bash
cd spring-boot/11-virtual-threads/virtual-threads

# Option A — Docker Compose (recommended)
docker compose up --build

# Option B — plain docker build + run
docker build -t dockyard/virtual-threads:1.0.0 .
docker run --rm -p 8080:8080 dockyard/virtual-threads:1.0.0
```

## Endpoints at a glance

| Method | Path                       | Purpose                                                      |
|--------|----------------------------|----------------------------------------------------------------|
| GET    | `/api/demo/run`            | Run N simulated blocking tasks through ONE named executor       |
| GET    | `/api/demo/compare`        | Run the SAME workload through both executors, report speedup    |
| GET    | `/api/demo/request-thread` | Report which thread served THIS HTTP request (virtual or not)   |

## Verify

```bash
cd spring-boot/11-virtual-threads/virtual-threads
./mvnw test
./mvnw spring-boot:run
```