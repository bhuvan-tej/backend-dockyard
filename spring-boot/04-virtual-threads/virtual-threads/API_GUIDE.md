# 📖 Virtual Threads — API & Usage Guide

> **Base URL:** `http://localhost:8080/api`
>
> Start the app first: `./mvnw spring-boot:run` **or** `docker compose up --build`.

---

## 1. The headline demo: `/demo/compare`

Runs the SAME simulated workload (N tasks, each blocking for `delayMs`)
through both executors, back to back, in one response.

```bash
curl "http://localhost:8080/api/demo/compare?tasks=100&delayMs=200"
```

```
{
  "taskCount": 100,
  "delayMsPerTask": 200,
  "virtualRun":  {"executor": "VIRTUAL",  "totalWallClockMs": 210, "distinctThreadsUsed": 100, ...},
  "platformRun": {"executor": "PLATFORM", "totalWallClockMs": 1005, "distinctThreadsUsed": 20, ...},
  "speedupFactor": 4.79
}
```

Notice: `virtualRun.distinctThreadsUsed` ≈ `taskCount` (one virtual thread per
task), while `platformRun.distinctThreadsUsed` is capped at the pool size (20).
The platform run's total time scales with `tasks / poolSize`, the virtual
run's does not — try `tasks=500` and watch `speedupFactor` grow.

---

## 2. Run one executor at a time: `/demo/run`

```bash
# Virtual: fast regardless of task count
curl "http://localhost:8080/api/demo/run?executor=VIRTUAL&tasks=200&delayMs=150"

# Platform: queues once tasks exceed the pool size (20)
curl "http://localhost:8080/api/demo/run?executor=PLATFORM&tasks=200&delayMs=150"
```

Valid `executor` values: `VIRTUAL` (default), `PLATFORM`.
`tasks`: 1–10000 (default 100). `delayMs`: 0–10000 (default 200).

Sending an out-of-range value returns `400 Bad Request`:

```bash
curl -i "http://localhost:8080/api/demo/run?tasks=999999"
# HTTP/1.1 400
```

---

## 3. Which thread served THIS request? `/demo/request-thread`

```bash
curl "http://localhost:8080/api/demo/request-thread"
# {"threadName":"VirtualThread[#34]/runnable@ForkJoinPool-1-worker-3","virtualThread":true,"delayMs":0}
```

With `spring.threads.virtual.enabled: true` (set in `application.yml`), this
is **always** a virtual thread — Tomcat itself is configured to hand off every
request that way. Add a delay to simulate a slow request:

```bash
curl "http://localhost:8080/api/demo/request-thread?delayMs=500"
```

### See it under real concurrency

Fire many concurrent requests and watch none of them queue up waiting for a
free "connection handler thread" (there effectively isn't a fixed pool to run
out of):

```bash
for i in $(seq 1 50); do
  curl -s "http://localhost:8080/api/demo/request-thread?delayMs=300" &
done
wait
```

All 50 should return in roughly 300ms total, not 50× that.

---

## 4. Watching it happen live: the thread dump

While a big `/demo/compare` or `/demo/run` request is in flight (use a large
`tasks` + `delayMs` so it takes a few seconds), hit the thread dump endpoint
in another terminal:

```bash
curl "http://localhost:8080/api/actuator/threaddump" | grep -c '"virtual" : true'
```

You'll see a burst of `"virtual" : true` entries while the virtual-thread run
executes, and a small, fixed number of `platform-pool-N` named threads while
the platform run executes.

---

## Error shape

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more query parameters are out of the allowed range",
  "path": "/api/demo/run",
  "timestamp": "2026-08-02T10:15:30"
}
```