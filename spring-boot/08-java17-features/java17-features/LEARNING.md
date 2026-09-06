# LEARNING — Java 12-17 Features, Explained

This is the narrative companion to the endpoints — read this to understand
**why** each feature was added and how it actually works, not just what it
returns.

## 1. Why this app picks up where Java 11 left off

Java 11 (September 2018) was the LTS most teams migrated to from Java 8.
**Java 17 (September 2021) is the next LTS after it**, and it is where the
"modern Java" style really begins: records, sealed types and pattern
matching together change how you *model* a domain, not just how you write a
loop.

Everything in versions 12 through 17 shipped on the 6-month cadence, so
"Java 17 features" in an interview almost always means "everything between
12 and 17". That is exactly what this app covers.

| Feature                                                           | JEP | Standard in                   |
|-------------------------------------------------------------------|-----|-------------------------------|
| Switch expressions                                                | 361 | Java 14                       |
| Helpful NullPointerExceptions                                     | 358 | Java 14 (on by default in 15) |
| Text blocks                                                       | 378 | Java 15                       |
| Pattern matching for `instanceof`                                 | 394 | Java 16                       |
| Records                                                           | 395 | Java 16                       |
| `Stream.toList()`                                                 | —   | Java 16                       |
| Sealed classes and interfaces                                     | 409 | **Java 17**                   |
| `RandomGenerator` API                                             | 356 | **Java 17**                   |
| `Collectors.teeing`, compact numbers, `String.indent`/`transform` | —   | Java 12                       |
| `String.formatted`                                                | —   | Java 15                       |

> **One honest caveat.** Pattern matching *in a `switch`* (`case Circle c ->`)
> was a **preview** feature in Java 17 (JEP 406) and only became standard in
> Java 21 (JEP 441). This repo compiles with **Java 21**, so the examples run
> as written. On a strict Java 17 compiler you'd need `--enable-preview`, or
> a chain of `if (shape instanceof Circle c)`. Sealing and pattern matching
> were designed together — 17 is where the pieces landed, 21 is where the
> last one was finished.

## 2. Records — transparent carriers for immutable data

A record declares the **state**, and the compiler derives the API from it:

```java
public record Point(int x, int y) { }
```

That single line generates:

- `private final int x;` and `private final int y;`
- a **canonical constructor** `Point(int x, int y)`
- **accessors** `x()` and `y()` — note: *not* `getX()`/`getY()`. A record is
  not a JavaBean.
- `equals`/`hashCode` derived from **all** components, so
  `new Point(3,4).equals(new Point(3,4))` is `true`
- `toString()` → `Point[x=3, y=4]`

Roughly 40 lines of boilerplate (or a Lombok `@Value`) replaced by a
*language* feature — no annotation processor, and the compiler, javadoc,
IDEs and pattern matching all understand it natively.

### The compact constructor

This is the single most-asked record detail:

```
public record Point(int x, int y) {
    public Point {                       // no parameter list, no assignments
        if (x < 0 || y < 0) throw new IllegalArgumentException(...);
    }
}
```

It runs **before** the implicit field assignments, so reassigning a
parameter inside it changes what actually gets stored. That makes it the
idiomatic home for both **validation** and **normalisation** (trimming a
string, copying a collection, clamping a number).

### The rules

| Can                                                | Cannot                                                          |
|----------------------------------------------------|-----------------------------------------------------------------|
| Implement interfaces                               | Extend a class (it already extends `java.lang.Record`)          |
| Declare **static** fields and methods              | Declare extra **instance** fields                               |
| Add extra instance methods                         | Be `abstract`, or non-`final` (implicitly `final`)              |
| Override any generated member                      | Assign to a field inside a compact constructor                  |
| Be declared **locally**, inside a method (Java 16) | Serve well as a JPA entity (needs no-arg ctor + mutable fields) |

### The trap: records are only *shallowly* immutable

The generated fields are `final`, which stops **reassignment** — not
mutation of what they point at:

```
List<String> guests = new ArrayList<>(List.of("alice"));
var r = new Reservation("Table 4", guests);
guests.add("gatecrasher");     // did this change r?
```

It does, unless the compact constructor copies first —
`guests = List.copyOf(guests);`. Same story for arrays and `Date`. See
`GET /java17/records/shallow-immutability`.

### Record vs Lombok `@Data`

A record is a *language* feature and immutable by design. Lombok's `@Data`
generates mutable getters/setters via an annotation processor. Use records
for immutable value types, DTOs and API responses; you still need a regular
class for JPA entities and anything genuinely mutable.

## 3. Sealed classes and interfaces

Before Java 17 you had two blunt options: `final` (nobody may extend) or
open (anybody may). There was no way to say **"these three types and no
others"** — which is exactly what modelling a domain needs. A payment is
CARD or UPI or NETBANKING; a shape is a circle, square or rectangle.

```java
public sealed interface Shape permits Shape.Circle, Shape.Square, Shape.Rectangle { }
```

### Why the compiler cares

A sealed hierarchy is a **closed set**, so a `switch` over it can be checked
for **exhaustiveness**. Cover all three subtypes and you need no `default`
branch — and if a fourth shape is ever added, every such switch stops
compiling until you handle it. A `default` branch would have swallowed the
new case *silently at runtime* instead. That compile-time nudge is the
entire payoff.

### The rules

- Every permitted subtype must be in the same **module** (same **package**
  in the unnamed module) and must extend/implement the sealed type
  **directly** — you cannot permit a grandchild.
- Every permitted subtype must itself declare **`final`**, **`sealed`** or
  **`non-sealed`**. There is no unspecified option; the compiler forces the
  decision.
  - `final` — the hierarchy stops here.
  - `sealed` — it continues, but still under control.
  - `non-sealed` — it is deliberately reopened. This is the escape hatch
    that stops sealing being all-or-nothing.
- `permits` may be **omitted** when all subtypes live in the same source
  file (they're then inferred) — which is why `Shape`'s nested records could
  drop it.
- `sealed`, `permits` and `non-sealed` are **contextual keywords**, so
  `int sealed = 1;` still compiles. `non-sealed` is the only hyphenated
  keyword in Java.
- The permitted list is stored **in the class file**, so
  `Class.getPermittedSubclasses()` can read it back — the endpoints here do
  exactly that.

### Records + sealed = algebraic data types

Records are implicitly `final`, which satisfies sealing's subtype rule for
free. A sealed interface of records is Java's version of a sum type: "a
`Shape` is *one of* exactly these three". Combined with pattern matching it
replaces both the visitor pattern and the long `instanceof`-else chain.

## 4. Pattern matching for `instanceof`

The old three-step dance — test, cast, assign — collapses into one:

```
if (o instanceof String) { String s = (String) o; ... }   // before
if (o instanceof String s) { ... }                        // after
```

`s` is a **pattern variable**, and you can use it in the *same* condition:
`o instanceof String s && !s.isBlank()` works, because by the time `&&`
evaluates its right side the compiler knows the match succeeded.

### Flow scoping — the rule that surprises everyone

A pattern variable is in scope exactly where the compiler can **prove** the
match succeeded. That includes the code *after* a negated guard clause:

```
if (!(o instanceof String s)) return;
// 's' IS in scope here — the only way to reach this line is if the match passed
System.out.println(s.length());
```

The mirror image fails: `if (o instanceof String s || s.isEmpty())` is a
compile error, because with `||` the compiler cannot prove `s` was bound.

## 5. Switch expressions

A `switch` that **produces a value**:

```java
String kind = switch (day) {
    case "SATURDAY", "SUNDAY" -> "WEEKEND";
    case "MONDAY", "TUESDAY"  -> "WEEKDAY";
    default -> "UNKNOWN";
};
```

- **Arrow labels never fall through.** No `break`, and no accidental
  fall-through bug. Several constants can share one branch.
- **`yield`** returns a value from a multi-statement `{ ... }` branch.
  `return` inside a switch *expression* is a compile error — it would be
  ambiguous whether you meant to leave the switch or the enclosing method.
- A switch **expression must be exhaustive**: over an enum, cover every
  constant or add `default`; over a sealed type, cover every permitted
  subtype.
- The old colon-label **statement** form still works, unchanged. This is an
  addition, not a replacement — but you cannot mix arrow and colon labels in
  the same switch.

A subtle one worth knowing: adding `default` to a switch over an enum
*loses* you the compile error you actually wanted when a new constant is
added later. Prefer exhaustive cases without `default`.

## 6. Text blocks

Multi-line string literals delimited by `"""`:

```java
String sql = """
        SELECT id, name, email
        FROM users
        WHERE active = true""";
```

### What the compiler does for you

1. **Incidental whitespace is stripped.** The compiler finds the minimum
   indentation across all non-blank lines *and the closing delimiter line*,
   then removes exactly that much from every line. Indentation you add to
   keep the source readable never reaches the runtime String — but anything
   deeper is *essential* and is preserved. Moving the closing `"""` further
   **left therefore adds** indentation to the result.
2. **Trailing whitespace on every line is stripped** — always, and
   invisibly.
3. **Line terminators are normalised to `\n`**, so the same source produces
   the same String on Windows and Unix.

### The two new escapes

- `\` at end of line — **suppresses that line break**, letting one logical
  line be wrapped across several source lines.
- `\s` — a literal space that **survives** the trailing-whitespace
  stripping.

Quotes need no escaping inside a text block; only **three consecutive** `"`
do (`\"""`).

### What it is *not*

Still just a `String` — same type, same interning, and **no interpolation**.
Java has no `${...}`. Use `String.formatted(...)` (Java 15), which is
`String.format` turned into an instance method so the template stays first:

```
"""
Hello, %s! You have %d messages.
""".formatted(name, count)
```

## 7. Stream, Collector and String additions

### `Stream.toList()` (Java 16)

Terser than `collect(Collectors.toList())`, but the difference that matters
is the **contract**. Three list flavours people constantly conflate:

| How you built it | Mutable? | Nulls allowed? |
|---|---|---|
| `Collectors.toList()` | yes (an `ArrayList` in practice — *not guaranteed*) | yes |
| `Stream.toList()` | **no** — unmodifiable | **yes** |
| `Collectors.toUnmodifiableList()` / `List.of()` | no | **no** — throws NPE |

`Arrays.asList(...)` is a fourth: fixed-size but element-mutable.

### `Collectors.teeing(c1, c2, merger)` (Java 12)

Runs **two** downstream collectors over a **single** traversal, then merges
their results:

```java
double avg = numbers.stream().collect(Collectors.teeing(
        Collectors.summingDouble(x -> x),
        Collectors.counting(),
        (sum, count) -> sum / count));
```

It exists because a stream can be consumed only **once** — before Java 12
you had to collect to a list and traverse twice, or fall back on
`summaryStatistics()`, which only covers a fixed set of aggregates. Teeing
composes *any* two collectors.

### String helpers

| Method                                | Since | What it does                                                                                            |
|---------------------------------------|-------|---------------------------------------------------------------------------------------------------------|
| `formatted(Object...)`                | 15    | `String.format` as an instance method — reads far better after a text block                             |
| `indent(int)`                         | 12    | Adds *n* leading spaces (or removes, if negative), normalises line endings, **appends a trailing `\n`** |
| `transform(Function)`                 | 12    | Applies an arbitrary function inline, keeping a chain left-to-right instead of nesting inside-out       |
| `stripIndent()`, `translateEscapes()` | 15    | Apply text-block processing to an ordinary String at runtime                                            |

## 8. Runtime and JDK additions

### Helpful NullPointerExceptions (JEP 358)

Before Java 14, an NPE gave you a class name, a line number, and nothing
else. On `a.getB().getC().getD()` you couldn't tell *which* link was null
without a debugger. Now the JVM reconstructs the failing expression from the
bytecode:

```
Cannot invoke "String.toUpperCase()" because the return value of
"java.util.Map.get(Object)" is null
```

Two details worth carrying into an interview: the message is computed
**lazily**, only when `getMessage()` is called, so there is no cost unless
an NPE actually happens and is inspected — and it can **leak field and
variable names into logs**, which matters if you ship stack traces to a
third party. Introduced in 14 behind
`-XX:+ShowCodeDetailsInExceptionMessages`, **on by default since Java 15**.

### The `RandomGenerator` API (JEP 356, Java 17)

`java.util.Random` hard-wired a single 48-bit LCG from 1995.
`RandomGenerator` turns the **algorithm** into a runtime choice looked up by
name, adds stream-producing methods, and defines sub-interfaces
(`JumpableGenerator`, `SplittableGenerator`, …) for parallel and simulation
workloads that need statistically independent substreams. `Random`,
`SplittableRandom` and `SecureRandom` were all retrofitted to implement it,
so existing code keeps working.

```
RandomGeneratorFactory.of("L64X128MixRandom").create(seed).ints(5, 0, 100)
```

Two gotchas: `RandomGenerator.of(name)` is **unseeded** (use the factory +
`create(seed)` when you need reproducibility), and **none of these are
cryptographically secure** unless you're using `SecureRandom` — don't
generate tokens with `L64X128MixRandom`.

### Compact number formatting (Java 12)

Locale-aware `"1M"` (SHORT) and `"1 million"` (LONG) formatting built
straight into `java.text.NumberFormat` — the thing every dashboard used to
hand-roll with if/else chains and hard-coded suffixes. Always pass an
explicit `Locale`; the output is locale-sensitive by design.

## 9. How the pieces fit together

The three headline features are not independent — they were designed as one
arc:

> **Records** give you the data. **Sealed types** close the set of shapes
> that data can take. **Pattern matching** destructures it, and the compiler
> checks you covered every case.

That combination is what lets you replace a visitor-pattern class hierarchy,
or a fragile `instanceof`-else ladder, with a single exhaustive `switch`
that the compiler re-verifies every time the domain changes. Java 21's
record patterns (`case Circle(double r) ->`) are the next step on the same
road — which is where `spring-boot/09-java21-features` would pick up.