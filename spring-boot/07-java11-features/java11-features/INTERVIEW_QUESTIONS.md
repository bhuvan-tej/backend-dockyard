# Interview Questions — Java 9-11 Features

Standalone question-by-question prep sheet. Each answer links to the
endpoint that demonstrates it live — run it, don't just memorize it.

---

### Q1. What does `var` actually do, and what is it NOT?

It's local-variable type inference (JEP 286, Java 10) — the compiler
determines ONE concrete static type from the initializer expression at
COMPILE time and bakes it into the bytecode. It is NOT dynamic typing, NOT
`Object`, and NOT "weak typing" — `var list = new ArrayList<String>()`
produces bytecode identical to `ArrayList<String> list = ...`. → `GET
/java11/var/local-variable`

### Q2. List four places `var` CANNOT be used.

Fields, regular method parameters (non-lambda), method return types, and
generic type arguments (`List<var>`). Also illegal: an uninitialized local,
`var x = null;` with no cast, and an untyped lambda/method reference with no
target type. → `GET /java11/var/pitfalls`

### Q3. Why was `var` extended to lambda parameters in Java 11 specifically?

JEP 323 — so lambda parameters could carry annotations. A bare, type-less
lambda parameter (`x -> ...`) has no annotation target at all; before Java
11 you'd have had to spell out the FULL parameter type just to attach an
annotation. `var` lets you keep type inference while still allowing the
annotation. All parameters in the list must consistently use `var` — you
cannot mix `var` and an explicit type. → `GET /java11/var/lambda-params`

### Q4. `isBlank()` vs `isEmpty()` — what's the actual difference?

`isEmpty()` only checks `length() == 0`. `isBlank()` additionally treats a
string made ENTIRELY of whitespace as blank, even if its length is > 0. →
`GET /java11/string/is-blank`

### Q5. Why does `strip()` exist when `trim()` already existed?

`trim()` only strips characters `<= U+0020` (a decision baked into Java
since version 1.0). `strip()`/`stripLeading()`/`stripTrailing()` use
`Character.isWhitespace(char)`, which correctly recognizes Unicode
whitespace characters ABOVE U+0020 too (e.g. U+2003 EM SPACE), which
`trim()` silently leaves behind. → `GET /java11/string/strip-variants`

### Q6. Are ALL "space-looking" Unicode characters removed by `strip()`?

No — `Character.isWhitespace()` deliberately EXCLUDES a few non-breaking
space-like characters: U+00A0 (NBSP), U+2007 (FIGURE SPACE), and U+202F
(NNBSP). `strip()` leaves those behind too, by design — they're meant to be
non-breaking, i.e. NOT treated as ordinary whitespace.

### Q7. What does `String.repeat(int)` replace?

The older idiom of `String.join("", Collections.nCopies(count, value))`, or
a manual `StringBuilder` loop — `"ab".repeat(3)` produces `"ababab"`
directly. → `GET /java11/string/repeat`

### Q8. How does `String.lines()` differ from splitting with a regex like `\n`?

`lines()` handles `\n`, `\r\n`, AND `\r` as line terminators automatically,
without you writing a regex that has to account for all three — useful for
text that may have mixed line-ending styles. → `GET /java11/string/lines`

### Q9. `Predicate.not(predicate)` vs `predicate.negate()` — when does `not()` actually matter?

Both produce an equivalent negated predicate. `not()` matters specifically
for negating a bare METHOD REFERENCE directly:
`list.stream().filter(Predicate.not(String::isBlank))` — `String::isBlank`
has no `.negate()` you can call until it's already assigned to a
`Predicate` variable. → `GET /java11/functional/predicate-not`

### Q10. Why was `Optional.isEmpty()` added when `!isPresent()` already worked?

Pure readability — `if (opt.isEmpty())` reads more naturally than
`if (!opt.isPresent())`. It's exactly the logical inverse of `isPresent()`,
nothing more. → `GET /java11/functional/optional-is-empty`

### Q11. What does `Collection.toArray(IntFunction<T[]>)` give you over `toArray(new T[0])`?

Functionally, in modern JVMs, very little performance-wise (the JIT already
optimizes the zero-length-array idiom well) — the real benefit is
readability: `list.toArray(String[]::new)` reads clearly as "give me a
`String[]`," and the generator is guaranteed to produce a correctly-sized,
correctly-typed array in one step. → `GET /java11/functional/collection-to-array`

### Q12. What problem does `Path.of(...)` solve that `Paths.get(...)` didn't?

None, functionally — `Paths.get(...)` still works and now just delegates to
`Path.of` internally. The point of adding `Path.of` was API ergonomics:
`Path` construction no longer needs to go through a SEPARATE class
(`Paths`) than the type it actually produces (`Path`). → `GET
/java11/nio/path-of`

### Q13. What do `Files.readString`/`writeString` replace?

Manual `BufferedReader`/`BufferedWriter` (or `InputStreamReader`/
`OutputStreamWriter`) boilerplate just to read/write a whole file as a
single `String` — or a third-party helper like Apache Commons IO's
`FileUtils.readFileToString`. Both are now one-line calls in the JDK
itself. → `GET /java11/nio/read-write-string`

### Q14. What replaced `HttpURLConnection`, and when was it standardized?

`java.net.http.HttpClient` — an incubator module in Java 9/10, STANDARDIZED
in Java 11 (JEP 321). It removes the need to manually manage connection
objects, output streams, and response-code plumbing that
`HttpURLConnection` required. → `GET /java11/httpclient/synchronous`

### Q15. `HttpClient.send()` vs `sendAsync()` — what's the actual difference?

`send(request, bodyHandler)` BLOCKS the calling thread until the full
response arrives. `sendAsync(request, bodyHandler)` returns a
`CompletableFuture<HttpResponse<T>>` IMMEDIATELY — the calling thread is
never blocked, and you compose further stages (`thenApply`, `thenAccept`,
...) exactly like any other `CompletableFuture`. → `GET
/java11/httpclient/synchronous` vs `/asynchronous`

### Q16. Why does Java 11 matter so much for real-world migrations, if 9 and 10 already existed?

Java 9, 10, and 12+ (until 17) were all non-LTS releases with a short
support window — most production teams stayed on Java 8 until Java 11
(September 2018), the next LONG-TERM-SUPPORT release, and jumped straight
from 8 to 11, absorbing everything 9/10/11 added at once. That's exactly
why this app groups all of it together as "the step after Java 8."

