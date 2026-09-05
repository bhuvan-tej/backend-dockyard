# Interview Questions — Core Java 8 Features

Standalone question-by-question prep sheet. Each answer links to the
endpoint that demonstrates it live — run it, don't just memorize it.

---

### Q1. What is a lambda expression, and what is it "shorthand" for?

An anonymous function — parameters, `->`, and a body — that implements a
functional interface's single abstract method inline, without the ceremony
of a named class or an anonymous inner class. It has no meaning on its own;
it always needs a **target type** (a functional interface) for the compiler
to check it against. → `GET /java8/lambda/two-arg/expression-body`

### Q2. What's the difference between a lambda's expression body and block body?

An expression body (`(a, b) -> a + b`) has no braces, no `return` keyword,
and the expression's value IS the return value. A block body
(`(a, b) -> { int t = a + b; return t; }`) can hold multiple statements but
**requires** an explicit `return` to produce a value. → `GET
/java8/lambda/two-arg/expression-body` vs `/two-arg/block-body`

### Q3. What does "effectively final" mean, and why does it matter for lambdas?

A local variable that is never reassigned after its initial assignment,
whether or not it's marked `final`. A lambda can only capture local variables
that are effectively final — it captures the **value** at creation time, not
a live, mutable reference. Reassigning the variable anywhere after the lambda
could observe it is a compile error. → `GET /java8/lambda/effectively-final-capture`

### Q4. How does a lambda differ from an anonymous inner class?

A lambda has no `this` of its own — `this` refers to the *enclosing* class
instance. An anonymous class has its own `this` and can declare fields and
extra methods, which a lambda cannot. Lambdas are typically compiled with
`invokedynamic` (no per-lambda `.class` file); anonymous classes always
produce a separate compiled class.

### Q5. What makes an interface a "functional interface"?

Exactly one abstract method (default and static methods don't count, since
they already have bodies). `@FunctionalInterface` is optional — the compiler
infers it from the shape — but it gives you a compile error the instant a
second abstract method is added by mistake. → `GET
/java8/functional-interfaces/custom`

### Q6. Name the four core `java.util.function` interfaces and their signatures.

`Predicate<T>` — `boolean test(T t)`. `Function<T,R>` — `R apply(T t)`.
`Supplier<T>` — `T get()`. `Consumer<T>` — `void accept(T t)`. → `GET
/java8/functional-interfaces/predicate`, `/function`, `/supplier`, `/consumer`

### Q7. `Function.andThen` vs `Function.compose` — what's the actual difference?

`f.andThen(g)` applies `f` first, then feeds the result into `g`:
`g.apply(f.apply(x))`. `f.compose(g)` applies `g` first, then feeds the
result into `f`: `f.apply(g.apply(x))`. Same two functions, reversed
evaluation order. → `GET /java8/functional-interfaces/function`

### Q8. Do `Predicate.and()`/`or()`/`negate()` mutate the original predicate?

No — each returns a brand-new `Predicate`, leaving both originals untouched
and independently reusable. → `GET /java8/functional-interfaces/predicate`

### Q9. List the four kinds of method reference with an example of each.

1. Static — `Integer::parseInt`. 2. Bound instance —
`existingString::toUpperCase`. 3. Unbound instance — `String::toUpperCase`
(instance supplied as the first parameter at call time). 4. Constructor —
`StringBuilder::new`. → `GET /java8/method-references/*`

### Q10. What's the difference between a "bound" and "unbound" instance method reference?

Bound: the instance is a specific, already-existing object, fixed at the
time the reference is created (`particularObject::method`). Unbound: no
instance is fixed in advance — it becomes the FIRST parameter of the
functional interface, supplied fresh at each call (`ClassName::method`). →
`GET /java8/method-references/bound-instance-method` vs
`/unbound-instance-method`

### Q11. Why were `default` methods added to interfaces in Java 8?

To let library authors add new methods to WIDELY-implemented interfaces
without breaking every existing implementer — recompiling old code would
otherwise fail because it doesn't implement the new abstract method. This is
exactly how `Collection.stream()` and `Iterable.forEach()` were retrofitted
onto every collection class ever written. → `GET
/java8/interface-methods/default-method`

### Q12. Can a `default` method be overridden? Can a `static` interface method?

Yes to default methods — implementers can override them exactly like any
other inherited method. No to static interface methods — they belong to the
interface TYPE itself, are called as `InterfaceName.method()`, and are never
inherited or overridable by implementing classes. → `GET
/java8/interface-methods/default-method-overridden` vs `/static-method`

### Q13. What is the "diamond problem" for default methods, and how does Java resolve it?

If a class implements two interfaces that declare a default method with the
SAME signature, Java refuses to guess which one you meant — it's a compile
error until the class explicitly overrides the method, typically combining
both via `InterfaceName.super.method()`. Unlike C++'s diamond problem, this
can only ever be a behavior conflict (interfaces have no fields/state to
inherit), and Java forces an explicit, visible resolution rather than
picking a winner silently. → `GET /java8/interface-methods/diamond-problem`

### Q14. Why does `Optional` exist, and where should it (and shouldn't it) be used?

To make "this might not have a value" part of the type system instead of a
convention you can forget to check with `null`. Its intended use is almost
exclusively as a METHOD RETURN TYPE — using it as a field type, a
constructor parameter, or inside a collection is generally discouraged (it
adds an allocation and boxing overhead without the same benefit).

### Q15. `Optional.of(x)` vs `Optional.ofNullable(x)` — when does each throw?

`Optional.of(x)` throws `NullPointerException` IMMEDIATELY if `x` is null —
it asserts "this is definitely non-null," so a null there is a bug, caught
at the earliest possible point. `Optional.ofNullable(x)` is null-safe — it
just becomes `Optional.empty()`. → `GET /java8/optional/creation-variants`

### Q16. `orElse(x)` vs `orElseGet(supplier)` — what's the practical difference?

`orElse(x)` evaluates `x` EAGERLY, as a normal Java method argument, on
EVERY call — even when the Optional is present and `x` is thrown away
unused. `orElseGet(supplier)` only invokes the supplier if the Optional
turns out to be empty. If the fallback is expensive (a DB call, a network
call), always prefer `orElseGet` to avoid paying for work you don't need. →
`GET /java8/optional/or-else-vs-or-else-get`

### Q17. What does `orElseThrow(Supplier<X>)` give you over plain `.get()`?

`.get()` throws a generic `NoSuchElementException` with a fixed message if
the Optional is empty. `orElseThrow(supplier)` lets you throw ANY exception
type with a custom, meaningful message — the caller controls exactly what
failure looks like. → `GET /java8/optional/or-else-throw`

### Q18. What problems in `java.util.Date`/`Calendar` did `java.time` fix?

`Date`/`Calendar` were MUTABLE (every setter mutates in place — shared-state
bugs), NOT thread-safe, used 0-indexed months (`Calendar.JANUARY == 0`, a
classic off-by-one trap), and `Date` conflated "a date," "a time," and "an
instant" into one confusing type. `java.time` gives one IMMUTABLE type per
concept instead. → `GET /java8/datetime/local-date`

### Q19. `Period` vs `Duration` — how do you choose?

`Period.between(date1, date2)` is date-based (years/months/days) and
CALENDAR-AWARE — it correctly handles varying month lengths and leap years.
`Duration.between(time1, time2)` is time-based (hours/minutes/seconds/
nanos), a fixed length with NO calendar awareness. Use `Period` for
"how many days/months/years apart," `Duration` for "how much elapsed time." →
`GET /java8/datetime/period-vs-duration`

### Q20. `LocalDateTime` vs `ZonedDateTime` vs `Instant` — what's each one for?

`LocalDateTime` — a date+time with NO time zone attached (a "wall-clock"
reading, ambiguous about which zone it's in). `ZonedDateTime` — the same,
PLUS an explicit zone, so it represents an actual, unambiguous moment as a
human in that zone would read it. `Instant` — a single point on the UTC
timeline with no zone concept at all (a raw machine timestamp) — the type
you want for "when did this actually happen," independent of any observer's
local time. → `GET /java8/datetime/zoned-and-instant`

### Q21. Why is `DateTimeFormatter` preferred over the old `SimpleDateFormat`?

`SimpleDateFormat` is NOT thread-safe — it mutates an internal `Calendar` on
every `format`/`parse` call, so sharing one instance across threads silently
corrupts results under concurrency (a bug so common it has its own
Stack Overflow folklore). `DateTimeFormatter` is immutable, so a single
instance can safely be shared and reused everywhere. → `GET
/java8/datetime/formatter`

