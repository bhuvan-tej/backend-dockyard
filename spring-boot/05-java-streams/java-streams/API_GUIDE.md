# 📖 Java Streams — API & Usage Guide

> **Base URL:** `http://localhost:8080/api`
>
> Start the app first: `./mvnw spring-boot:run` **or** `docker compose up --build`.
>
> Every response has the same shape:
> `{ "operation": ..., "description": ..., "codeSnippet": ..., "result": ... }`
> — read `LEARNING.md` for the full narrative behind each one.

---

## 1. Stream creation — `/streams/creation/*`

```bash
curl "http://localhost:8080/api/streams/creation/collection"
curl "http://localhost:8080/api/streams/creation/varargs"
curl "http://localhost:8080/api/streams/creation/array"
curl "http://localhost:8080/api/streams/creation/int-range?start=0&end=10"
curl "http://localhost:8080/api/streams/creation/generate?count=5"
curl "http://localhost:8080/api/streams/creation/iterate?seed=1&count=8"
curl "http://localhost:8080/api/streams/creation/empty"
curl "http://localhost:8080/api/streams/creation/concat"
```

`generate` and `iterate` are backed by **infinite** streams — try omitting
`limit` mentally: without it, these would never terminate. `count` is capped
at 100 by validation so nobody accidentally asks for an infinite response.

---

## 2. Intermediate operations — `/streams/intermediate/*`

```bash
curl "http://localhost:8080/api/streams/intermediate/filter?department=Engineering"
curl "http://localhost:8080/api/streams/intermediate/map"
curl "http://localhost:8080/api/streams/intermediate/flatmap"
curl "http://localhost:8080/api/streams/intermediate/distinct"
curl "http://localhost:8080/api/streams/intermediate/sorted"
curl "http://localhost:8080/api/streams/intermediate/sorted-comparator"
curl "http://localhost:8080/api/streams/intermediate/peek"
curl "http://localhost:8080/api/streams/intermediate/limit?count=3"
curl "http://localhost:8080/api/streams/intermediate/skip?count=3"
```

The `peek` endpoint is the most instructive one — its `result.executionTrace`
shows entries interleaved as `"passed filter: X"`, `"after map: X"`,
`"passed filter: Y"`, `"after map: Y"`, ... proving elements travel through
the WHOLE pipeline one at a time, not phase by phase.

---

## 3. Terminal operations — `/streams/terminal/*`

```bash
curl "http://localhost:8080/api/streams/terminal/foreach"
curl "http://localhost:8080/api/streams/terminal/count?department=Finance"
curl "http://localhost:8080/api/streams/terminal/max"
curl "http://localhost:8080/api/streams/terminal/min"
curl "http://localhost:8080/api/streams/terminal/anymatch?threshold=100000"
curl "http://localhost:8080/api/streams/terminal/allmatch?minAge=18"
curl "http://localhost:8080/api/streams/terminal/nonematch?floor=50000"
curl "http://localhost:8080/api/streams/terminal/findfirst?department=Sales"
curl "http://localhost:8080/api/streams/terminal/findany?city=Mumbai"
curl "http://localhost:8080/api/streams/terminal/reduce-no-identity"
curl "http://localhost:8080/api/streams/terminal/reduce-identity"
curl "http://localhost:8080/api/streams/terminal/reduce-combiner?threshold=100000"
```

Compare `reduce-no-identity` (returns an `Optional`-wrapped value) against
`reduce-identity` (returns a plain value) to see exactly why the identity
argument changes the return type's shape.

---

## 4. Collectors — `/streams/collectors/*`

```bash
curl "http://localhost:8080/api/streams/collectors/tolist"
curl "http://localhost:8080/api/streams/collectors/toset"
curl "http://localhost:8080/api/streams/collectors/tomap"
curl "http://localhost:8080/api/streams/collectors/tomap-merge"
curl "http://localhost:8080/api/streams/collectors/joining"
curl "http://localhost:8080/api/streams/collectors/groupingby"
curl "http://localhost:8080/api/streams/collectors/groupingby-counting"
curl "http://localhost:8080/api/streams/collectors/groupingby-mapping"
curl "http://localhost:8080/api/streams/collectors/groupingby-nested"
curl "http://localhost:8080/api/streams/collectors/partitioningby?yearsThreshold=5"
curl "http://localhost:8080/api/streams/collectors/summarizing"
curl "http://localhost:8080/api/streams/collectors/averaging"
curl "http://localhost:8080/api/streams/collectors/summing"
curl "http://localhost:8080/api/streams/collectors/tocollection"
curl "http://localhost:8080/api/streams/collectors/maxby"
curl "http://localhost:8080/api/streams/collectors/reducing"
```

`groupingby-nested` returns a genuinely two-level report — department, then
city within department — worth comparing side-by-side with the flat
`groupingby` response to see exactly what "nesting" adds.

---

## 5. Primitive streams — `/streams/primitives/*`

```bash
curl "http://localhost:8080/api/streams/primitives/maptoint-sum"
curl "http://localhost:8080/api/streams/primitives/maptoint-average"
curl "http://localhost:8080/api/streams/primitives/maptoint-max"
curl "http://localhost:8080/api/streams/primitives/summary-stats-int"
curl "http://localhost:8080/api/streams/primitives/summary-stats-double"
curl "http://localhost:8080/api/streams/primitives/boxed"
curl "http://localhost:8080/api/streams/primitives/rangeclosed-sum?bound=20"
```

`IntStream`/`DoubleStream` come with terminal operations (`sum`, `average`,
`summaryStatistics`) a plain `Stream<Employee>` simply doesn't have —
`maptoint-sum` and `summary-stats-int` show them running directly off
`mapToInt(Employee::age)`. `rangeclosed-sum` shows an `IntStream` built and
consumed with no object `Stream` involved at all.

---

## 6. Parallel streams — `/streams/parallel/*`

```bash
# CPU-bound speedup demo — try a few different sizes
curl "http://localhost:8080/api/streams/parallel/compare?elements=5000"
curl "http://localhost:8080/api/streams/parallel/compare?elements=200000"

# The shared-mutable-state data race — run it a few times, watch the unsafe size vary
curl "http://localhost:8080/api/streams/parallel/pitfall?count=50000"
```

With `elements=5000`, `speedupFactor` may be close to 1 (or even below —
splitting overhead can outweigh a small workload). With `elements=200000`,
expect a noticeably higher `speedupFactor` on a multi-core machine.

For the pitfall endpoint, run it several times:

```bash
for i in 1 2 3; do curl -s "http://localhost:8080/api/streams/parallel/pitfall?count=50000" | grep -o '"unsafeForEachResultSize":[0-9]*'; done
```

You should see `unsafeForEachResultSize` occasionally come back **below**
50000 (lost updates from the data race), while `safeCollectResultSize` is
always exactly 50000.

---

## 7. Post-Java 8 additions — `/streams/modern/*`

```bash
curl "http://localhost:8080/api/streams/modern/tolist"
curl "http://localhost:8080/api/streams/modern/takewhile?year=2018"
curl "http://localhost:8080/api/streams/modern/dropwhile?year=2018"
curl "http://localhost:8080/api/streams/modern/teeing"
curl "http://localhost:8080/api/streams/modern/ofnullable?ids=1,2,999,15"
curl "http://localhost:8080/api/streams/modern/optional-stream?ids=1,2,999,15"
curl "http://localhost:8080/api/streams/modern/mapmulti"
curl "http://localhost:8080/api/streams/modern/filtering?salaryThreshold=100000"
curl "http://localhost:8080/api/streams/modern/iterate-predicate?seed=1&bound=1000"
```

Every other controller in this app deliberately sticks to Java 8-era
methods only — this is the one place those later additions (Java 9–16)
live, each paired against the Java 8 equivalent it improves on. See
LEARNING.md section 10 for the full "why leave these out elsewhere" story.

Worth trying side by side:
- `ofnullable` vs `optional-stream` — both skip id `999` (which doesn't
  exist), via two different mechanisms for the same "flatten away the
  misses" goal.
- `mapmulti` vs `/streams/intermediate/flatmap` — same department+city tag
  output, two different mechanisms (flatMap allocates a Stream per element;
  mapMulti doesn't).
- `filtering` vs `/streams/collectors/groupingby` — `filtering` keeps every
  department key even if its filtered list ends up empty; a plain
  `filter()` applied before grouping would have dropped that key entirely.

---

## Error shape

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more query parameters are out of the allowed range",
  "path": "/api/streams/creation/generate",
  "timestamp": "2026-08-02T10:15:30"
}
```