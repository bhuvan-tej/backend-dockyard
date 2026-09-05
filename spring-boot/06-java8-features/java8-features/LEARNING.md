# LEARNING— Core Java 8 Features, Explained

This is the narrative companion to the endpoints — read this to understand
**why** each feature was added and how it actually works, not just what it
returns.

## 1. Why Java 8 mattered

Before Java 8, Java had no concise way to pass "a bit of behavior" around as
data — you needed a full anonymous inner class just to sort a list with a
custom comparator. Java 8 (2014) closed that gap with **lambda expressions**
and **method references**, backed by a new **functional interface** concept,
and used those same primitives to retrofit `default`/`static` methods onto
existing interfaces (making the Streams API possible without breaking every
collection class ever written), a null-safe `Optional` type, and a completely
new date/time API. This app covers everything except the Stream API itself,
which has its own dedicated app (`spring-boot/05-java-streams`).

## 2. Lambda expressions

A lambda is an anonymous function: parameters, an arrow `->`, and a body.

```
(int a, int b) -> { return a + b; }   // explicit types, block body
(a, b) -> { return a + b; }           // inferred types, block body
(a, b) -> a + b                       // inferred types, expression body (implicit return)
a -> a * 2                            // single param — parens optional
() -> 42                              // no params
```

A lambda has no meaning on its own — it's always assigned to a **target
type**, which must be a functional interface (see below). The compiler infers
the lambda's parameter types from that target interface's abstract method.

### Variable capture — "effectively final"

A lambda can read a local variable from its enclosing scope only if that
variable is never reassigned after its initial assignment — it doesn't need
the `final` keyword, just the *property* of never changing (hence
"effectively final"). The lambda captures the **value** at creation time, not
a live reference — so this is a compile error:

```
int count = 0;
Runnable r = () -> System.out.println(count);
count++; // ERROR: variable count must be final or effectively final
```

### Lambda vs. anonymous inner class

They look similar but are not the same thing:
- A lambda has **no `this` of its own** — `this` inside a lambda refers to
  the *enclosing* class instance. An anonymous class has its own `this`.
- A lambda **cannot declare fields** or additional methods; an anonymous
  class can.
- Lambdas are typically compiled using `invokedynamic` (no per-lambda
  `.class` file); anonymous classes always compile to a separate
  `Outer$1.class` file.

## 3. Functional interfaces

A **functional interface** is any interface with **exactly one abstract
method** (default and static methods don't count — they already have a
body). That single method is the lambda's "shape" — its parameter types and
return type. `@FunctionalInterface` is optional but recommended: it gives you
a compile error the moment a second abstract method sneaks in.

The JDK ships a whole family in `java.util.function`:

| Interface           | Abstract method       | Use for                                       |
|---------------------|-----------------------|-----------------------------------------------|
| `Predicate<T>`      | `boolean test(T t)`   | a yes/no question about a value               |
| `Function<T,R>`     | `R apply(T t)`        | transform T into R                            |
| `Supplier<T>`       | `T get()`             | lazily produce a value, no input              |
| `Consumer<T>`       | `void accept(T t)`    | do something with a value, no output          |
| `BiFunction<T,U,R>` | `R apply(T t, U u)`   | like Function, but two inputs                 |
| `UnaryOperator<T>`  | `T apply(T t)`        | a Function where input and output types match |
| `BinaryOperator<T>` | `T apply(T t1, T t2)` | a BiFunction where all three types match      |

All of them support **composition** methods that return a **new**
function/predicate rather than mutating either operand:
`Predicate.and/or/negate`, `Function.andThen/compose`, `Consumer.andThen`.

`andThen` vs `compose` is the classic trip-up: `f.andThen(g)` applies `f`
first, then feeds the result into `g`. `f.compose(g)` applies `g` first, then
feeds the result into `f` — the same two functions, opposite evaluation
order.

## 4. Method references — shorthand for "just call an existing method"

A method reference is a lambda that does nothing but call one already-named
method. There are four distinct shapes:

| Kind                | Syntax                             | Equivalent lambda                               |
|---------------------|------------------------------------|-------------------------------------------------|
| 1. Static           | `ClassName::staticMethod`          | `args -> ClassName.staticMethod(args)`          |
| 2. Bound instance   | `particularObject::instanceMethod` | `args -> particularObject.instanceMethod(args)` |
| 3. Unbound instance | `ClassName::instanceMethod`        | `(obj, args) -> obj.instanceMethod(args)`       |
| 4. Constructor      | `ClassName::new`                   | `args -> new ClassName(args)`                   |

The compiler determines which kind applies purely from the **target
functional interface's method signature** — `String::toUpperCase` can be kind
3 (`Function<String,String>`, taking the instance as the first/only
parameter) in one context, while `someString::toUpperCase` (kind 2) fixes the
instance in advance.

## 5. Interface evolution: default & static methods

Before Java 8, an interface was a pure contract — 100% abstract methods.
Adding a method to a widely-implemented interface broke every existing
implementer. Java 8 fixed this two ways:

- **`default` methods** — an instance method WITH a body, inherited by every
  implementer that doesn't override it. This is exactly how
  `Collection.stream()` and `Iterable.forEach()` were retrofitted onto every
  collection class ever written, without recompiling or breaking any of
  them.
- **`static` methods** — a utility method that belongs to the interface TYPE
  itself, called as `InterfaceName.method()`, never through an instance, and
  never inherited or overridable.

### The diamond problem

If a class implements two interfaces that both declare a default method with
the **same signature**, Java refuses to guess which one you meant — it's a
**compile error** until the class overrides the method itself, typically
combining both via `InterfaceName.super.method()`:

```
interface Flyer  { default String move() { return "flies"; } }
interface Swimmer { default String move() { return "swims"; } }
class FlyingFish implements Flyer, Swimmer {
    public String move() { return Flyer.super.move() + " AND " + Swimmer.super.move(); }
}
```

This is Java's deliberate, safer alternative to C++'s multiple-inheritance
diamond problem: interfaces carry no state (no fields), so only *behavior*
conflicts can occur, and Java forces you to resolve them explicitly, in code,
rather than picking a winner silently.

## 6. Optional — an explicit alternative to null

`Optional<T>` makes "this might not have a value" part of the **type**
itself, instead of a convention you can forget to check. It is intended
almost exclusively as a **method return type**, not as a field type or a
method parameter type.

- `Optional.of(x)` — asserts `x` is definitely non-null; throws
  `NullPointerException` **immediately** if it's null (fail fast, at
  creation).
- `Optional.ofNullable(x)` — the safe, general-purpose choice; becomes
  `Optional.empty()` if `x` was null.
- `Optional.empty()` — explicitly "nothing here."

### The classic gotcha: `orElse` vs `orElseGet`

```
optional.orElse(expensiveFallback());        // expensiveFallback() ALWAYS runs — it's a normal Java method argument
optional.orElseGet(() -> expensiveFallback()); // only runs if optional turns out to be empty
```

`orElse(x)` evaluates `x` **eagerly**, on every call, whether the
Optional is present — because in Java, method arguments are evaluated before
the method is invoked. `orElseGet(supplier)` defers that work into a lambda
that only runs when actually needed. If the fallback is expensive (a DB
call, a network call), always prefer `orElseGet`.

## 7. java.time — replacing Date and Calendar

The old `java.util.Date`/`Calendar` API had real, well-known problems:
**mutable** objects (every setter mutates in place — a classic source of
shared-state bugs), **not thread-safe**, 0-indexed months (`Calendar.JANUARY
== 0`, a legendary source of off-by-one bugs), and `Date` conflating "a
date," "a time," and "an instant" into one confusing type.

`java.time` (JSR-310) fixes all of this with one **immutable** type per
concept:

| Type            | Represents                                                              |
|-----------------|-------------------------------------------------------------------------|
| `LocalDate`     | A date, no time or zone (a birthday)                                    |
| `LocalTime`     | A time, no date or zone (a daily alarm)                                 |
| `LocalDateTime` | Both, still no zone (a local wall-clock reading)                        |
| `ZonedDateTime` | Date + time + zone (an actual moment, as a human reads it, somewhere)   |
| `Instant`       | A single point on the UTC timeline, zone-agnostic (a machine timestamp) |

Every "mutator" (`plusDays`, `withYear`, ...) returns a **new** instance —
the original is never touched, which is what makes these types thread-safe
by construction.

### Period vs Duration

- `Period.between(date1, date2)` — **date-based** (years/months/days),
  calendar-aware: correctly accounts for varying month lengths and leap
  years.
- `Duration.between(time1, time2)` — **time-based** (hours/minutes/seconds/
  nanos), a fixed length of time with **no** calendar awareness at all.

### DateTimeFormatter

Replaces the old `SimpleDateFormat`, which was famously **not** thread-safe
(it mutated an internal `Calendar` on every call — a shared instance used
concurrently would silently corrupt results). `DateTimeFormatter` instances
are immutable and safe to share across every thread/request.

## Further reading

- *Java 8 in Action* (Urma, Fusco, Mycroft) — the definitive guide to this
  exact feature set.
- [JSR-310: Date and Time API](https://jcp.org/en/jsr/detail?id=310) — the
  formal spec behind `java.time`.
- `java.util.function` package Javadoc — every built-in functional interface,
  in one place.