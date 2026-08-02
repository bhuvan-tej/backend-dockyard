# 📚 Virtual Threads — the concepts

## The problem virtual threads solve

Traditional Java concurrency uses **platform threads** — a thin wrapper
around a real OS thread. Each one costs roughly 1MB of stack memory and real
OS scheduling overhead. Practically, a single JVM can run a few thousand of
these at once before running out of memory or grinding under context-switch
overhead.

That ceiling is fine for CPU-bound work, but most server-side code is
**I/O-bound**: waiting on a database query, an HTTP call to another service,
a file read. A "thread-per-request" web server handling many *slow,
concurrent, I/O-bound* requests runs out of threads long before it runs out
of CPU — most of those threads are just sitting there blocked, waiting.

The traditional fix was to rewrite that code in a **reactive** style
(WebFlux, Project Reactor, callbacks/`CompletableFuture` chains) so one small
pool of threads could juggle many in-flight operations without blocking any
of them. That works, but it's a fundamentally different, harder-to-read
programming model — stack traces get harder to follow, debugging gets
harder, and every library in the call chain has to be reactive too.

## What a virtual thread actually is

A **virtual thread** (JEP 444, finalized/stable in Java 21) is a
JVM-managed thread that is NOT tied 1:1 to an OS thread. Instead:

- Many virtual threads share a small pool of real OS threads, called
  **carrier threads**.
- When a virtual thread's code calls a **blocking** operation that the JDK
  has been taught to recognize — `Thread.sleep`, blocking I/O
  (`java.net.Socket`, `java.io.*`), JDBC drivers, `java.util.concurrent`
  locks, etc. — the JVM **unmounts** that virtual thread from its carrier and
  parks it, cheaply, off to the side. The now-free carrier thread immediately
  goes and runs a DIFFERENT virtual thread's code.
- When the blocking operation completes, the virtual thread gets
  **remounted** onto some available carrier thread and picks up exactly where
  it left off — as far as the code is concerned, nothing unusual happened; it
  was just a normal blocking call.

The result: you write **completely normal, sequential, blocking-style Java**
— no reactive types, no callback pyramids — and the JVM gets async-style
scalability for free, because "blocked" virtual threads cost almost nothing
and never tie up a real OS thread while they wait.

## Why this app's demo works (and what it's actually proving)

`DownstreamSimulator.call()` does one thing: `Thread.sleep(delayMs)`. It's the
simplest possible stand-in for "any blocking call" — and critically,
`Thread.sleep` is one of the operations the JVM has been taught to unmount a
virtual thread for.

- `ThreadingConfig.virtualThreadExecutor()` — `Executors.newVirtualThreadPerTaskExecutor()`
  creates a **new virtual thread for every task**, with no pool to size.
  Submit 10,000 tasks that each sleep 200ms, and they all effectively run
  "at once" — the carrier threads (as many as your CPU has cores, roughly)
  just keep hopping between whichever virtual threads are currently NOT
  asleep.
- `ThreadingConfig.platformThreadExecutor()` — a plain, deliberately small
  `Executors.newFixedThreadPool(20)`. Submit 100 tasks that each sleep 200ms,
  and only 20 can run at a time; the rest queue. Total wall-clock time grows
  roughly linearly with `tasks / poolSize`.
- `/demo/compare` runs the *identical* workload through both and reports the
  ratio — see **[API_GUIDE.md](API_GUIDE.md)** for real numbers.

## The one crucial limitation: virtual threads don't make CPUs faster

If your task is CPU-bound — a tight loop doing math, not waiting on
anything — it never calls a blocking operation, so it never unmounts, so it
just occupies its carrier thread the whole time, same as a platform thread
would. Virtual threads are a scalability win for **I/O-bound, highly
concurrent** workloads specifically — they are not a free performance
multiplier for compute-bound code. This is exactly why `DownstreamSimulator`
uses `Thread.sleep` (I/O-bound stand-in) and not a CPU-bound loop.

## The application-level switch: `spring.threads.virtual.enabled`

Since Spring Boot 3.2, setting this single property to `true` makes:

- The embedded Tomcat connector hand every incoming HTTP request to its own
  new virtual thread (instead of borrowing one from Tomcat's own fixed
  `maxThreads` platform pool).
- `@Async` methods (if you use them) run on virtual threads too, via an
  auto-configured `AsyncTaskExecutor`.

This app sets it in `application.yml` and proves it with
`/demo/request-thread`, which reports `Thread.currentThread().isVirtual()`
for whatever thread is handling that exact request — see the note in
`ThreadDemoControllerTest` about why this can only be proven with a REAL
HTTP round trip (`webEnvironment = RANDOM_PORT`), not MockMvc, which
dispatches on the calling thread and never touches Tomcat's connector at all.

## Interview quick-hits

- **"What is a virtual thread?"** — A cheap, JVM-managed thread that is not
  permanently bound to an OS thread; it "unmounts" from its carrier platform
  thread during blocking operations, letting the carrier run other virtual
  threads meanwhile.
- **"How is this different from a thread pool?"** — A thread pool reuses a
  *fixed* number of OS threads and queues excess work. Virtual threads don't
  need a fixed pool at all — you can create one per task, because they're
  cheap enough that millions can exist concurrently.
- **"Does this replace reactive programming (WebFlux)?"** — For most I/O-bound
  server workloads, yes — you get similar scalability with much simpler,
  ordinary blocking code. Reactive programming is still relevant for
  extremely high-throughput streaming/backpressure scenarios.
- **"What DOESN'T benefit from virtual threads?"** — CPU-bound work. A virtual
  thread that never blocks never unmounts — it behaves just like a platform
  thread.
- **"How do you enable virtual threads for an entire Spring Boot web app?"** —
  `spring.threads.virtual.enabled=true` (Spring Boot 3.2+, Java 21+).

