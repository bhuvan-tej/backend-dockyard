# API Guide — Core Java 8 Features

All endpoints live under the `/api` context path. Base URL used below:
`http://localhost:8080/api`.

Every endpoint returns a `Java8DemoResponse`:

```json
{
  "operation": "what this endpoint demonstrates",
  "description": "plain-English explanation of why it works this way",
  "codeSnippet": "the exact Java that produced the result",
  "result": { }
}
```

---

## 1. Lambda expressions (`/java8/lambda`)

### `GET /java8/lambda/no-arg`
```bash
curl "http://localhost:8080/api/java8/lambda/no-arg"
```
`() -> {}` — no parameters, a block body, no return value (`Runnable`).

### `GET /java8/lambda/single-param?name=Ada`
```bash
curl "http://localhost:8080/api/java8/lambda/single-param?name=Ada"
```
`n -> ...` — a single, inferred-type parameter; surrounding parens are
optional.

### `GET /java8/lambda/two-arg/expression-body?a=3&b=4`
```bash
curl "http://localhost:8080/api/java8/lambda/two-arg/expression-body?a=3&b=4"
```
`(a, b) -> a + b` — no braces, no `return`, no semicolon; the expression's
value is the return value.

### `GET /java8/lambda/two-arg/block-body?a=3&b=4`
```bash
curl "http://localhost:8080/api/java8/lambda/two-arg/block-body?a=3&b=4"
```
`(a, b) -> { ...; return ...; }` — multiple statements allowed, but an
explicit `return` is required.

### `GET /java8/lambda/effectively-final-capture?message=closures`
```bash
curl "http://localhost:8080/api/java8/lambda/effectively-final-capture?message=closures"
```
Demonstrates capturing an "effectively final" local variable — the value at
creation time, not a live reference.

---

## 2. Functional interfaces (`/java8/functional-interfaces`)

### `GET /java8/functional-interfaces/predicate?value=4`
```bash
curl "http://localhost:8080/api/java8/functional-interfaces/predicate?value=4"
```
`Predicate<Integer>` composed with `and`/`or`/`negate` — each call returns a
NEW predicate.

### `GET /java8/functional-interfaces/function?input=hello`
```bash
curl "http://localhost:8080/api/java8/functional-interfaces/function?input=hello"
```
`Function<String,Integer>` composed with `andThen` vs `compose` — same two
functions, reversed evaluation order.

### `GET /java8/functional-interfaces/supplier`
```bash
curl "http://localhost:8080/api/java8/functional-interfaces/supplier"
```
`Supplier<String>` — the body runs only when `.get()` is called, not before.

### `GET /java8/functional-interfaces/consumer?text=hello`
```bash
curl "http://localhost:8080/api/java8/functional-interfaces/consumer?text=hello"
```
`Consumer<String>` chained with `andThen` — both consumers run, in order,
against the same input.

### `GET /java8/functional-interfaces/bifunction?a=3&b=4`
```bash
curl "http://localhost:8080/api/java8/functional-interfaces/bifunction?a=3&b=4"
```
`BiFunction<Integer,Integer,Integer>` — a two-argument `Function`.

### `GET /java8/functional-interfaces/custom?a=3&b=4`
```bash
curl "http://localhost:8080/api/java8/functional-interfaces/custom?a=3&b=4"
```
A hand-rolled `@FunctionalInterface` (`Calculator`) used exactly like a
built-in one, including its own `default`/`static` methods.

---

## 3. Method references (`/java8/method-references`)

### `GET /java8/method-references/static-method?number=42`
```bash
curl "http://localhost:8080/api/java8/method-references/static-method?number=42"
```
Kind 1 — `Integer::parseInt`.

### `GET /java8/method-references/bound-instance-method?text=world`
```bash
curl "http://localhost:8080/api/java8/method-references/bound-instance-method?text=world"
```
Kind 2 — `particularObject::instanceMethod` (the instance is fixed already).

### `GET /java8/method-references/unbound-instance-method?words=alpha,beta,gamma`
```bash
curl "http://localhost:8080/api/java8/method-references/unbound-instance-method?words=alpha,beta,gamma"
```
Kind 3 — `String::toUpperCase` (the instance is supplied at call time).

### `GET /java8/method-references/constructor?seeds=alpha,beta`
```bash
curl "http://localhost:8080/api/java8/method-references/constructor?seeds=alpha,beta"
```
Kind 4 — `StringBuilder::new`.

### `GET /java8/method-references/two-arg-unbound?a=Hello&b=HELLO`
```bash
curl "http://localhost:8080/api/java8/method-references/two-arg-unbound?a=Hello&b=HELLO"
```
Kind 3, two-argument form — `String::equalsIgnoreCase` as a `BiFunction`.

---

## 4. Interface methods (`/java8/interface-methods`)

### `GET /java8/interface-methods/default-method`
```bash
curl "http://localhost:8080/api/java8/interface-methods/default-method"
```
A default method used as-is, inherited by a lambda that only supplies the
interface's one abstract method.

### `GET /java8/interface-methods/default-method-overridden`
```bash
curl "http://localhost:8080/api/java8/interface-methods/default-method-overridden"
```
The same default method, explicitly overridden by a different implementer.

### `GET /java8/interface-methods/static-method?name=Linus`
```bash
curl "http://localhost:8080/api/java8/interface-methods/static-method?name=Linus"
```
A static interface method — called on the interface type, never an instance.

### `GET /java8/interface-methods/diamond-problem`
```bash
curl "http://localhost:8080/api/java8/interface-methods/diamond-problem"
```
Two interfaces, one conflicting default method, resolved via
`InterfaceName.super.method()`.

---

## 5. Optional (`/java8/optional`)

### `GET /java8/optional/creation-variants?value=hi`
```bash
curl "http://localhost:8080/api/java8/optional/creation-variants?value=hi"
curl "http://localhost:8080/api/java8/optional/creation-variants"   # omit value → null case
```
`Optional.of` vs `Optional.ofNullable` vs `Optional.empty` — and the NPE risk
of `Optional.of(null)`.

### `GET /java8/optional/map-filter-pipeline?value=%20%20hello%20%20`
```bash
curl "http://localhost:8080/api/java8/optional/map-filter-pipeline?value=%20%20hello%20%20"
```
| Param   | Default     | Meaning                                                      |
|---------|-------------|--------------------------------------------------------------|
| `value` | `  hello  ` | Piped through `.map(trim).filter(!isEmpty).map(toUpperCase)` |

A step-by-step trace of which pipeline stages actually ran.

### `GET /java8/optional/or-else-vs-or-else-get?value=hi`
```bash
curl "http://localhost:8080/api/java8/optional/or-else-vs-or-else-get?value=hi"
curl "http://localhost:8080/api/java8/optional/or-else-vs-or-else-get"   # omit value → both side effects run
```
The classic gotcha: `orElse` runs its argument eagerly every time;
`orElseGet` only runs its supplier when the Optional is empty.

### `GET /java8/optional/or-else-throw?value=hi`
```bash
curl "http://localhost:8080/api/java8/optional/or-else-throw?value=hi"
curl "http://localhost:8080/api/java8/optional/or-else-throw"   # omit value → 404 with a custom message
```
`orElseThrow(exceptionSupplier)` — a custom exception instead of the generic
`NoSuchElementException` from plain `.get()`.

### `GET /java8/optional/if-present?value=hi`
```bash
curl "http://localhost:8080/api/java8/optional/if-present?value=hi"
```
`ifPresent(Consumer)` — runs only if a value exists, no exception either way.

---

## 6. java.time (`/java8/datetime`)

### `GET /java8/datetime/local-date`
```bash
curl "http://localhost:8080/api/java8/datetime/local-date"
```
`LocalDate` — immutable date-only type; every mutator returns a new instance.

### `GET /java8/datetime/local-time`
```bash
curl "http://localhost:8080/api/java8/datetime/local-time"
```
`LocalTime` — a time-of-day with no date or zone.

### `GET /java8/datetime/local-date-time`
```bash
curl "http://localhost:8080/api/java8/datetime/local-date-time"
```
`LocalDateTime` — date + time, still no zone.

### `GET /java8/datetime/period-vs-duration?start=2026-01-01&end=2026-03-15&startTime=09:00&endTime=17:30`
```bash
curl "http://localhost:8080/api/java8/datetime/period-vs-duration?start=2026-01-01&end=2026-03-15&startTime=09:00&endTime=17:30"
```
| Param          | Default  | Format                |
|----------------|----------|-----------------------|
| `start`, `end` | required | ISO date `yyyy-MM-dd` |
| `startTime`    | `09:00`  | `HH:mm`               |
| `endTime`      | `17:30`  | `HH:mm`               |

`Period.between` (calendar-aware, date-based) vs `Duration.between`
(fixed-length, time-based).

### `GET /java8/datetime/formatter?pattern=yyyy-MM-dd HH:mm:ss`
```bash
curl "http://localhost:8080/api/java8/datetime/formatter?pattern=yyyy-MM-dd%20HH:mm:ss"
```
`DateTimeFormatter` with a custom pattern — thread-safe and reusable, unlike
the old `SimpleDateFormat`.

### `GET /java8/datetime/zoned-and-instant`
```bash
curl "http://localhost:8080/api/java8/datetime/zoned-and-instant"
```
`Instant` (zone-agnostic) viewed through two different `ZonedDateTime` zones,
plus legacy `java.util.Date` interop via `Date.from()`/`toInstant()`.