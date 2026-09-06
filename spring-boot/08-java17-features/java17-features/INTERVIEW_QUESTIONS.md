# Interview Questions — Java 12-17 Features

Standalone question-by-question prep sheet. Each answer links to the
endpoint that demonstrates it live — run it, don't just memorize it.

---

## Records

### Q1. What exactly does the compiler generate for a record?

Private final fields for every component, a canonical constructor,
**accessors named after the components** (`x()`, not `getX()` — a record is
not a JavaBean), and `equals`/`hashCode`/`toString` derived from **all**
components. The class is implicitly `final` and implicitly extends
`java.lang.Record`. → `GET /java17/records/generated-members`

### Q2. What is a compact constructor and why does it exist?

A constructor written with **no parameter list and no field assignments**:
`public Point { ... }`. It runs **before** the implicit field assignments,
so reassigning a parameter inside it changes what actually gets stored. That
makes it the idiomatic place for validation *and* normalisation (trimming,
copying a collection, clamping). Assigning directly to `this.x` inside it is
a compile error. → `GET /java17/records/compact-constructor`

### Q3. Are records immutable?

**Shallowly.** The generated fields are `final`, which prevents
reassignment — but if a component is a mutable object (a `List`, an array, a
`Date`), the object it points at can still be mutated from outside. You need
a defensive copy in the compact constructor (`guests = List.copyOf(guests)`)
and, for a truly mutable type, in the accessor too. →
`GET /java17/records/shallow-immutability`

### Q4. Name four things a record cannot do.

Extend another class (it already extends `java.lang.Record`); declare
instance fields beyond its components (static fields are fine); be
`abstract` or non-`final`; assign to a field inside a compact constructor.
It **can** implement interfaces, add static/instance methods, and override
any generated member. → `GET /java17/records/limitations`

### Q5. Record vs Lombok `@Data` — which and when?

A record is a **language** feature: no annotation processor, understood
natively by the compiler, javadoc, IDEs and pattern matching, and immutable
by design. `@Data` generates mutable getters/setters via an annotation
processor. Use records for immutable value types, DTOs and API responses.
Use a regular class for JPA entities, which need a no-arg constructor and
mutable fields.

### Q6. Can a record be declared inside a method?

Yes — **local records** (Java 16). They're the clean replacement for the
throwaway tuple class (or the `Object[]` / `Map.Entry` hack) you used to
write just to carry an intermediate result through a stream pipeline. →
`GET /java17/records/local-record`

---

## Sealed classes and interfaces

### Q7. What problem do sealed types solve?

Before them you had only `final` (nobody may extend) or open (anybody may).
Sealing adds the missing middle ground — **"exactly these types and no
others"** — which is what modelling a real domain needs: a payment is CARD
or UPI or NETBANKING, and nothing else. →
`GET /java17/sealed/shape-hierarchy`

### Q8. What is the actual practical benefit, beyond documentation?

**Exhaustiveness checking.** Because the compiler knows the complete set of
subtypes, a `switch` covering all of them needs no `default` branch — and
adding a fourth subtype later breaks the build until every such switch
handles it. A `default` branch would have swallowed the new case silently at
runtime. → `GET /java17/sealed/exhaustive-switch`

### Q9. What must every permitted subtype declare?

One of **`final`**, **`sealed`** or **`non-sealed`** — there is no
unspecified option. `final` stops the hierarchy, `sealed` continues it under
control, `non-sealed` deliberately reopens it (the escape hatch that stops
sealing being all-or-nothing). `non-sealed` is the only hyphenated keyword
in Java. → `GET /java17/sealed/vehicle-hierarchy`

### Q10. When can you omit the `permits` clause?

When every permitted subtype is declared in the **same source file** — the
compiler infers the list. Otherwise subtypes must be in the same **module**
(same **package** in the unnamed module) and must extend/implement the
sealed type **directly**; you cannot permit a grandchild.

### Q11. Is sealing enforced at runtime or only at compile time?

**Both.** The permitted list is written into the class file, so it can't be
bypassed with a hand-rolled class file — and `Class.getPermittedSubclasses()`
can read it back, which is exactly what these endpoints do.

### Q12. Are `sealed` and `permits` reserved words?

No — they're **contextual keywords**, so existing code with a variable named
`sealed` still compiles. Java has avoided adding true reserved words for
years for exactly this reason. → `GET /java17/sealed/pitfalls`

---

## Pattern matching and switch

### Q13. What does pattern matching for `instanceof` remove?

The test-cast-assign dance. `if (o instanceof String s)` tests, casts and
binds in one expression, and `s` can be used in the same condition —
`o instanceof String s && !s.isBlank()` compiles, because by the time `&&`
evaluates its right side the match is known to have succeeded. →
`GET /java17/pattern-matching/instanceof`

### Q14. What is flow scoping, and why does this compile?

```java
if (!(o instanceof String s)) return;
System.out.println(s.length());   // 's' is in scope here!
```
A pattern variable is in scope exactly where the compiler can **prove** the
match succeeded — and the only way to reach the line after that guard clause
is if it did. The mirror image fails:
`if (o instanceof String s || s.isEmpty())` is a compile error, because with
`||` the binding can't be proven. → `GET /java17/pattern-matching/flow-scoping`

### Q15. Switch expression vs switch statement?

A switch **expression** produces a value and must be **exhaustive**; a
switch **statement** doesn't. Arrow labels (`case A, B -> ...`) never fall
through, need no `break`, and allow several constants per branch. The old
colon form still works unchanged — but you cannot mix arrow and colon labels
in the same switch. → `GET /java17/pattern-matching/switch-expression`

### Q16. When do you need `yield`, and why not `return`?

`yield` produces the value of a **multi-statement block branch** in a switch
**expression**. `return` is a compile error there because it would be
ambiguous whether you meant to leave the switch or the enclosing method. A
switch *statement* with arrow labels needs no `yield` at all. →
`GET /java17/pattern-matching/yield`

### Q17. Why is adding `default` to a switch over an enum sometimes a bug?

Because it destroys the compile error you actually want. Without `default`,
adding a new enum constant breaks every non-exhaustive switch at compile
time — exactly where you want to be told. With `default`, the new constant
quietly falls into it at runtime. Same argument applies to sealed types.

### Q18. Pattern matching in `switch` — which Java version?

**Preview** in Java 17 (JEP 406), standard in **Java 21** (JEP 441). This is
the honest answer to "is `case Circle c ->` a Java 17 feature" — sealing is
final in 17, but the switch patterns it was designed for only finished in
21. This repo compiles with Java 21, so the demos run as written. →
`GET /java17/pattern-matching/sealed-dispatch`

### Q19. How do records + sealed + pattern matching fit together?

Records give you the **data**, sealed types **close the set** of shapes that
data can take, and pattern matching **destructures** it with the compiler
verifying every case is covered. Together they replace both the visitor
pattern and the fragile `instanceof`-else ladder with one exhaustive
`switch` that is re-checked every time the domain changes.

---

## Text blocks

### Q20. How does a text block decide what indentation to strip?

It finds the **minimum indentation across all non-blank lines *and* the
closing delimiter line**, then removes exactly that much from every line.
Indentation you added purely for source readability is "incidental" and
disappears; anything deeper is "essential" and is kept. Moving the closing
`"""` further **left adds** indentation to the result — the delimiter line
takes part in the calculation. →
`GET /java17/text-blocks/incidental-whitespace`

### Q21. What do the `\` and `\s` escapes do?

A trailing `\` **suppresses that line break**, so a long single logical line
can be wrapped across several source lines. `\s` is a literal space that
**survives** the automatic trailing-whitespace stripping applied to every
line. → `GET /java17/text-blocks/escapes`

### Q22. Does a text block add a trailing newline?

Only if the closing `"""` is on its **own line**. Put it at the end of the
last content line and there's no trailing `\n`. Line terminators are also
normalised to `\n` regardless of the platform the file was written on.

### Q23. Do text blocks support string interpolation?

**No.** Java has no `${...}`. A text block is just a `String` — same type,
same interning. Use `"...".formatted(a, b)` (Java 15), which is
`String.format` as an instance method so the template stays first, which
reads much better after a multi-line block. →
`GET /java17/text-blocks/formatted`

### Q24. How do you put three double quotes inside a text block?

Escape one of them: `\"""`. One or two consecutive quotes need no escaping
at all — only three, since that's the delimiter.

---

## API additions

### Q25. `Stream.toList()` vs `collect(Collectors.toList())` — the real difference?

The **contract**. `toList()` returns an **unmodifiable** list;
`Collectors.toList()` guarantees nothing about type or mutability (it
happens to give you an `ArrayList`). Relying on that being mutable is a bug
waiting to happen. → `GET /java17/api/stream-to-list`

### Q26. Is `Stream.toList()` the same as `Collectors.toUnmodifiableList()`?

No — and this is the follow-up question. Both are unmodifiable, but
`toList()` **permits null elements** while `toUnmodifiableList()` (and
`List.of`, and `List.copyOf`) throw `NullPointerException` on them.

### Q27. Why does `Collectors.teeing` exist?

Because a stream can be consumed only **once**. Teeing runs two downstream
collectors over a single traversal and merges their two results — before
Java 12 you had to collect to a list and traverse it twice, or fall back on
`summaryStatistics()`, which only covers a fixed set of aggregates. Teeing
composes *any* two collectors. → `GET /java17/api/teeing`

### Q28. What's surprising about `String.indent(n)`?

It isn't a plain prefix operation: it **normalises line terminators and
appends a trailing `\n`**, and a **negative** `n` removes up to *n* leading
spaces rather than adding them. → `GET /java17/api/string-helpers`

---

## Runtime and JDK

### Q29. What changed about NullPointerExceptions in Java 14?

JEP 358 — the JVM now reconstructs the failing expression from the bytecode:
`Cannot invoke "String.toUpperCase()" because the return value of
"java.util.Map.get(Object)" is null`. Previously a chained call like
`a.getB().getC()` gave you only a line number, with no way to tell which
link was null. → `GET /java17/jdk/helpful-npe`

### Q30. Is there a performance cost to helpful NPE messages?

No, in practice. The message is computed **lazily**, only when
`getMessage()` is called — so nothing is paid unless an NPE actually happens
*and* is inspected. Introduced behind
`-XX:+ShowCodeDetailsInExceptionMessages` in Java 14, **on by default since
Java 15**. The real caveat isn't speed, it's that the messages can leak
field and variable names into logs you ship elsewhere.

### Q31. What does the `RandomGenerator` API (Java 17) change?

`java.util.Random` hard-wired a single 48-bit LCG from 1995.
`RandomGenerator` makes the **algorithm** a runtime choice looked up by
name, adds stream-producing methods, and defines sub-interfaces
(`Jumpable`, `Splittable`, …) for parallel/simulation workloads needing
statistically independent substreams. `Random`, `SplittableRandom` and
`SecureRandom` were retrofitted to implement it, so old code needs no
migration. → `GET /java17/jdk/random-generator`

### Q32. Two traps with `RandomGenerator`?

`RandomGenerator.of(name)` is **unseeded** — use
`RandomGeneratorFactory.of(name).create(seed)` when you need reproducibility.
And **none** of the new generators are cryptographically secure; use
`SecureRandom` for tokens, passwords or anything security-relevant. →
`GET /java17/jdk/pitfalls`

### Q33. Why does Java 17 matter so much for real-world migrations?

Java 12–16 were all non-LTS with a six-month support window, so most
production teams went 8 → 11 → **17**. Java 17 is also the floor for the
modern ecosystem: **Spring Boot 3.x requires Java 17 minimum**, as do recent
Hibernate and Jakarta EE releases. That, plus records + sealed + pattern
matching arriving together, is why "Java 17 features" is really "everything
from 12 to 17" in an interview.

