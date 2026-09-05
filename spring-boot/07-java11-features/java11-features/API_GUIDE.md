# API Guide — Java 9-11 Features

All endpoints live under the `/api` context path. Base URL used below:
`http://localhost:8080/api`.

Every endpoint returns a `Java11DemoResponse`:

```json
{
  "operation": "what this endpoint demonstrates",
  "description": "plain-English explanation of why it works this way",
  "codeSnippet": "the exact Java that produced the result",
  "result": { }
}
```

---

## 1. var (`/java11/var`)

### `GET /java11/var/local-variable`
```bash
curl "http://localhost:8080/api/java11/var/local-variable"
```
Several `var` declarations, each paired with the concrete type the compiler
actually inferred.

### `GET /java11/var/enhanced-for-loop`
```bash
curl "http://localhost:8080/api/java11/var/enhanced-for-loop"
```
`var` as an enhanced for-loop variable, inferring the element type from the
collection's generic parameter.

### `GET /java11/var/lambda-params?a=3&b=4`
```bash
curl "http://localhost:8080/api/java11/var/lambda-params?a=3&b=4"
```
`var` on lambda parameters (Java 11, JEP 323) — added so they can carry
annotations.

### `GET /java11/var/try-with-resources`
```bash
curl "http://localhost:8080/api/java11/var/try-with-resources"
```
`var` as a try-with-resources resource variable.

### `GET /java11/var/pitfalls`
```bash
curl "http://localhost:8080/api/java11/var/pitfalls"
```
Documents (as text, since these are all compile errors) every place `var`
CANNOT be used.

---

## 2. String methods (`/java11/string`)

### `GET /java11/string/is-blank?value=%20%20%20`
```bash
curl "http://localhost:8080/api/java11/string/is-blank?value=%20%20%20"
```
`isBlank()` vs `isEmpty()` — a whitespace-only string is blank but not
empty.

### `GET /java11/string/strip-variants`
```bash
curl "http://localhost:8080/api/java11/string/strip-variants"
```
`strip()`/`stripLeading()`/`stripTrailing()` vs the legacy `trim()`, using
U+2003 (EM SPACE) — real Unicode whitespace that `trim()` misses.

### `GET /java11/string/repeat?value=ab&count=3`
```bash
curl "http://localhost:8080/api/java11/string/repeat?value=ab&count=3"
```
| Param   | Default | Range |
|---------|---------|-------|
| `value` | `ab`    | any   |
| `count` | `3`     | 0–50  |

### `GET /java11/string/lines`
```bash
curl "http://localhost:8080/api/java11/string/lines"
```
Splits a mixed-line-ending string (`\n`, `\r\n`) via `String.lines()`.

---

## 3. Functional/collection additions (`/java11/functional`)

### `GET /java11/functional/predicate-not?input=%20%20%20`
```bash
curl "http://localhost:8080/api/java11/functional/predicate-not?input=%20%20%20"
```
`Predicate.not(String::isBlank)` vs `predicate.negate()` — same result,
different ergonomics for negating a bare method reference.

### `GET /java11/functional/optional-is-empty?value=hi`
```bash
curl "http://localhost:8080/api/java11/functional/optional-is-empty?value=hi"
curl "http://localhost:8080/api/java11/functional/optional-is-empty"   # omit value → empty case
```
`Optional.isEmpty()` — the readable inverse of `isPresent()`.

### `GET /java11/functional/collection-to-array?items=alpha,beta,gamma`
```bash
curl "http://localhost:8080/api/java11/functional/collection-to-array?items=alpha,beta,gamma"
```
`Collection.toArray(IntFunction<T[]>)` — `list.toArray(String[]::new)`.

---

## 4. java.nio.file (`/java11/nio`)

### `GET /java11/nio/path-of`
```bash
curl "http://localhost:8080/api/java11/nio/path-of"
```
`Path.of(...)` construction, `resolve()`, and `normalize()` — no actual file
I/O.

### `GET /java11/nio/read-write-string?content=hello%20world`
```bash
curl "http://localhost:8080/api/java11/nio/read-write-string?content=hello%20world"
```
`Files.writeString`/`Files.readString` round-tripped through a real,
auto-deleted temp file.

---

## 5. HttpClient (`/java11/httpclient`)

Both endpoints below hit a tiny embedded local HTTP server started
in-process by this app (see `HttpClientService`) — no external network
access is required.

### `GET /java11/httpclient/synchronous?value=hi`
```bash
curl "http://localhost:8080/api/java11/httpclient/synchronous?value=hi"
```
`HttpClient.send(...)` — blocks until the full response arrives.

### `GET /java11/httpclient/asynchronous?value=hi`
```bash
curl "http://localhost:8080/api/java11/httpclient/asynchronous?value=hi"
```
`HttpClient.sendAsync(...)` — returns a `CompletableFuture` immediately; the
calling thread is never blocked on the network.