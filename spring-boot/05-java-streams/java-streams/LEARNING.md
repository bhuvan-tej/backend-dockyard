# 📚 Java Streams — Explained in Detail

## 1. What a Stream actually is (and isn't)

A `Stream` is **not** a data structure. It holds no elements of its own —
unlike a `List`, you cannot index into it, and it has no size until you ask
a terminal operation to compute one. It is better thought of as a **recipe**
for producing a sequence of values, built from a **source** (a collection, an
array, a range of numbers, ...) plus a chain of **operations** describing
what to do with each value as it flows through.

Three consequences fall directly out of this:

- **A stream is single-use.** Once a terminal operation has consumed it,
  calling any method on that same stream again throws
  `IllegalStateException: stream has already been operated upon or closed`.
  If you need to run two different pipelines over the same data, get a fresh
  stream from the source each time (`list.stream()` again).
- **A stream doesn't "contain" anything until you ask.** `list.stream().filter(...)`
  does not filter anything yet — it just remembers that it should, once asked.
- **Streams compose functional-style code, not imperative loops.** Instead of
  a `for` loop with an external mutable accumulator, you describe the
  transformation declaratively: "take these, keep the ones matching X, turn
  each into Y, collect them into Z."

## 2. The pipeline model: source → intermediate operations → terminal operation

Every stream pipeline has exactly this shape:

```
source.stream()          // SOURCE
    .filter(...)          // INTERMEDIATE — lazy, returns a new Stream
    .map(...)             // INTERMEDIATE — lazy, returns a new Stream
    .collect(...)         // TERMINAL — eager, triggers execution, returns a real value
```

- **Intermediate operations** (`filter`, `map`, `flatMap`, `distinct`, `sorted`,
  `peek`, `limit`, `skip`) are **lazy**. Calling one just appends a step to
  the pipeline's description and immediately returns a new `Stream` — no
  actual work happens yet, and none of your lambdas run yet either.
- **Terminal operations** (`forEach`, `count`, `collect`, `reduce`, `anyMatch`,
  `findFirst`, ...) are what actually **triggers the whole pipeline to run**,
  end to end, in one pass. Only after you call a terminal operation do the
  `filter`/`map` lambdas actually execute.
- **Crucially, this happens ELEMENT BY ELEMENT, not phase by phase.** A
  common misconception is that `stream().filter(a).map(b).forEach(c)` runs
  `filter` over the *entire* collection, then `map` over the *entire* result,
  then `forEach` over that. It does not. Each element is pulled from the
  source and pushed through the **whole** pipeline — filter, then map, then
  forEach — before the *next* element is even looked at. `/streams/intermediate/peek`
  in this app proves this directly: the trace it returns interleaves
  "passed filter" and "after map" per-element, not as two separate blocks.

## 3. Stream creation — every way to get one (Java 8)

| Method                                       | Use when...                                                                  |
|----------------------------------------------|------------------------------------------------------------------------------|
| `collection.stream()`                        | You already have a `List`/`Set`/other `Collection`.                          |
| `Stream.of(a, b, c)`                         | You have a small, fixed, inline set of literal values.                       |
| `Arrays.stream(array)`                       | You have an array (primitive or object).                                     |
| `IntStream.range(start, endExclusive)`       | You want the Stream equivalent of a counting `for` loop.                     |
| `IntStream.rangeClosed(start, endInclusive)` | Same, but including the upper bound.                                         |
| `Stream.generate(Supplier)`                  | Each element is produced independently — **infinite**, must `.limit(n)`.     |
| `Stream.iterate(seed, UnaryOperator)`        | Each element derives from the previous one — **infinite**, must `.limit(n)`. |
| `Stream.empty()`                             | A valid, immediately-terminating zero-element stream, useful as a base case. |
| `Stream.concat(a, b)`                        | You want to chain two streams together, source order preserved.              |

See `/streams/creation/*` for a runnable example of each.

## 4. Intermediate operations, one at a time

- **`filter(Predicate)`** — keeps only elements where the predicate is true.
  **Stateless**: each element is judged completely independently of every
  other element.
- **`map(Function)`** — transforms each element **1-to-1** into something
  else (same count in as out). **Stateless**.
- **`flatMap(Function<T, Stream<R>>)`** — like `map`, but each element is
  turned into its **own stream**, and all of those little streams are then
  **flattened** into one. Use this whenever a plain `map` would leave you
  with a "stream of streams" (e.g. mapping each `Order` to its `List<LineItem>`
  and wanting one flat stream of all line items across all orders).
- **`distinct()`** — removes duplicates using `equals()`/`hashCode()`.
  **Stateful**: it has to remember every distinct value seen so far to know
  whether the next one is new.
- **`sorted()` / `sorted(Comparator)`** — sorts the stream. **Stateful** in
  the strongest sense: it cannot produce even its *first* output element
  until it has seen **every** element (you can't know what's smallest until
  you've looked at everything). `Comparator.comparing(...).reversed().thenComparing(...)`
  (both Java 8) composes multi-key sorts far more readably than a hand-rolled
  `compare()` method.
- **`peek(Consumer)`** — runs a side-effecting action on each element **as it
  passes through**, without changing the stream itself. Intended purely for
  **debugging a pipeline mid-flight**. Using `peek` to perform real work
  (e.g. mutating external state as your actual business logic) is a
  well-known anti-pattern: the JDK doesn't guarantee `peek` even runs at all
  if the JIT can prove the stream's result doesn't depend on it — never rely
  on it for anything other than looking.
- **`limit(n)`** — truncates to at most `n` elements. **Short-circuiting**:
  the pipeline can stop pulling from the source as soon as `n` elements have
  come through, which is what makes `Stream.iterate(...).limit(n)` on an
  infinite source actually terminate.
- **`skip(n)`** — discards the first `n` elements. Must still **internally**
  traverse (and discard) those `n` elements — it cannot "jump ahead" on an
  arbitrary source.

## 5. Terminal operations, one at a time

- **`forEach(Consumer)`** — runs a side-effecting action per element, returns
  nothing. **Not** short-circuiting — it always visits every element.
- **`count()`** — visits every element and returns how many there are. **Not**
  short-circuiting, even though the *answer* is just one number — it can't
  know the count without looking at everything (some sources can short-cut
  this internally, e.g. `Collection.size()`, but conceptually count() still
  requires a full pass).
- **`min(Comparator)` / `max(Comparator)`** — need an explicit `Comparator`
  (there's no "natural minimum" for an arbitrary object). Return `Optional`
  because an **empty** stream has no minimum/maximum to report.
- **`anyMatch(Predicate)`** — **short-circuits** at the first element
  satisfying the predicate; does not necessarily look at the rest.
- **`allMatch(Predicate)`** — **short-circuits** at the first element that
  **fails** the predicate (the first counter-example disproves "all").
- **`noneMatch(Predicate)`** — **short-circuits** at the first element that
  **does** satisfy the predicate (one match disproves "none").
- **`findFirst()`** — the first matching element in **encounter order**.
  Short-circuits as soon as one is found.
- **`findAny()`** — **any** matching element, not necessarily the first. On a
  sequential stream this usually behaves identically to `findFirst`, but the
  distinction matters on a **parallel** stream, where enforcing "the first
  one, no matter which thread found it" is more expensive than "any one that
  any thread found".
- **`reduce` — the three overloads:**
  1. `reduce(BinaryOperator<T>)` → `Optional<T>`. No starting value, so an
     empty stream has nothing to return — hence `Optional`.
  2. `reduce(T identity, BinaryOperator<T>)` → `T`. With an explicit seed, an
     empty stream just returns that seed — no `Optional` needed.
  3. `reduce(U identity, BiFunction<U,T,U> accumulator, BinaryOperator<U> combiner)`
     → `U`. Needed when the running result type (`U`) differs from the
     stream's element type (`T`) — e.g. reducing a stream of `Employee` down
     to an `int` count. The **combiner** merges two partial `U` results
     computed on different chunks of the stream; a **sequential** stream
     never actually calls it, but it must still be supplied and be
     **correct**, because a **parallel** stream absolutely will call it.

## 6. Collectors — turning a stream back into something useful

`Collectors` is how you go from "a lazy pipeline description" back to a
concrete `List`/`Set`/`Map`/summary you can actually use.

- **`toList()` / `toSet()`** — drain the stream into a new `List`/`Set`.
  `toSet()` de-duplicates via `equals()`/`hashCode()`; iteration order is not
  guaranteed.
- **`toMap(keyFn, valueFn)`** — builds a `Map`. **If two elements produce the
  same key, this throws `IllegalStateException`** — the two-argument form
  has no idea which value should win.
- **`toMap(keyFn, valueFn, mergeFn)`** — the three-argument form adds a
  **merge function** that resolves collisions instead of throwing (e.g. "keep
  whichever one has the higher salary"). Reach for this the moment your key
  function isn't provably unique.
- **`joining(delimiter, prefix, suffix)`** — concatenates a `Stream<String>`
  into one `String`, with optional wrapping — no manual `StringBuilder` loop.
- **`groupingBy(classifier)`** — buckets elements by a key function into a
  `Map<Key, List<Element>>`. This is the direct Stream equivalent of SQL's
  `GROUP BY`.
- **`groupingBy(classifier, downstream)`** — pairs the grouping with a
  **second** collector applied to each group's elements instead of always
  getting a raw `List` back — e.g. `groupingBy(dept, counting())` for a
  per-department headcount, or `groupingBy(dept, mapping(Employee::name, toList()))`
  to get just the names per group.
- **Nested `groupingBy`** — the downstream of one `groupingBy` can simply be
  *another* `groupingBy`, giving you a two-level report
  (`Map<Department, Map<City, List<Employee>>>`). Collectors **compose**.
- **`partitioningBy(predicate)`** — a **special case** of `groupingBy` where
  the classifier is a `Predicate`. The result **always** has exactly two
  keys, `true` and `false` — even if one side is completely empty. Prefer
  this over `groupingBy` whenever the split is genuinely binary; it reads
  clearer and guarantees both keys exist.
- **`summarizingDouble/Int/Long(fn)`** — one pass, five numbers back at once:
  count, sum, min, max, average.
- **`averagingInt/Double` / `summingInt/Double`** — narrower, single-purpose
  collectors when you only need one of those five numbers.
- **`mapping(fn, downstream)`** — adapts a downstream collector to work on a
  **transformed** value instead of the raw element — this is what lets
  `groupingBy` produce `List<String>` (just names) instead of `List<Employee>`
  per group.
- **`reducing(identity, mapper, op)`** — the collector-shaped equivalent of
  `Stream.reduce`. Rarely reached for directly in real code (`groupingBy(...,
  summingDouble(...))` usually reads better), but worth recognizing — several
  other collectors are built from it internally.
- **`summingDouble/Int/Long(fn)`** — narrower than `summarizingDouble`:
  returns just the ONE number (the sum) directly, instead of a whole
  summary-statistics object you'd then pull one field back out of.
- **`toCollection(supplier)`** — like `toList()`/`toSet()`, but lets you
  choose the EXACT concrete collection type to collect into (e.g. a
  `LinkedList` instead of whatever `toList()` happens to use internally) —
  reach for this whenever the caller specifically needs that type's
  behaviour.
- **`minBy(comparator)` / `maxBy(comparator)`** — the collector-shaped
  equivalents of `Stream.min`/`Stream.max`, most useful as the DOWNSTREAM of
  a `groupingBy`, where a plain `Stream.max` isn't directly reachable. Still
  `Optional`-wrapped, for the same reason `Stream.min`/`max` are: an empty
  group has nothing to report.

## 7. Primitive streams — `IntStream`/`LongStream`/`DoubleStream`

A `Stream<Integer>` secretly **boxes** every `int` into an `Integer` object —
wasted allocation and indirection for pure numeric work. `IntStream`/
`LongStream`/`DoubleStream` exist specifically to avoid that, and they come
with terminal operations a plain `Stream<T>` simply **doesn't have**, because
they only make sense for numbers:

- **`mapToInt`/`mapToLong`/`mapToDouble`** — switch a regular object `Stream`
  onto the matching primitive stream (e.g. `employees.stream().mapToInt(Employee::age)`).
- **`sum()`** — only exists on primitive streams; "sum of a stream of
  `Employee`" is meaningless, "sum of a stream of `int`" isn't.
- **`average()`** — returns `OptionalDouble` (a **primitive specialization**
  of `Optional`, avoiding boxing even for the wrapper itself), because an
  empty stream has no average to report.
- **`max()`/`min()`** — return `OptionalInt`/`OptionalLong`/`OptionalDouble`,
  mirroring `Stream.max`/`min` but without boxing the result.
- **`summaryStatistics()`** — one pass, all five numbers at once (count,
  sum, min, max, average) as an `IntSummaryStatistics`/`DoubleSummaryStatistics`
  value object — the primitive-stream sibling of `Collectors.summarizingInt`/
  `summarizingDouble`.
- **`.boxed()`** — converts BACK from a primitive stream to `Stream<Integer>`/
  etc., needed the moment you want the values in a generic collection like
  `List<Integer>` — a `List` can't hold raw primitives, only their boxed
  wrapper type.
- `IntStream.rangeClosed`/`range` also work entirely on their own, with no
  object stream involved at all — the classic "sum of even numbers between
  1 and n" style problem is a pure `IntStream` pipeline from start to finish.

See `/streams/primitives/*` for all of the above running against the shared
employee dataset.

## 8. Parallel streams — the OTHER kind of concurrency

`stream()` vs `parallelStream()` (equivalently `stream().parallel()`) splits
the work across the JVM's shared common `ForkJoinPool`, using as many threads
as your machine has CPU cores.

**Contrast this directly with `04-virtual-threads` in this repo**: virtual
threads help **I/O-bound** work (many threads mostly *waiting*, not
computing). Parallel streams help **CPU-bound** work (real computation, no
blocking, that can be split across cores). Neither is a general "make
everything faster" switch — each targets the *opposite* kind of bottleneck.
`/streams/parallel/compare` in this app deliberately uses a CPU-heavy
busy-loop (no `Thread.sleep`, no I/O) to demonstrate this honestly.

**When parallel streams help:** large, CPU-bound workloads over an
**efficiently-splittable** source (arrays, `ArrayList`, `IntStream` ranges —
things that can be divided into equal halves cheaply). **When they don't
help, or actively hurt:** small data (splitting/merging overhead outweighs
the work itself), sources that are expensive to split (e.g. a `LinkedList` or
an `Iterator`-based stream), or **any** I/O/blocking inside the pipeline
(which ties up shared common-pool threads that the rest of the JVM may also
be relying on for unrelated parallel work).

**The classic pitfall — shared mutable state:** calling
`list.add(x)` on a plain `ArrayList` from inside a **parallel** `forEach` is
a **data race**: multiple threads mutate the *same* list concurrently, with
no synchronization. `/streams/parallel/pitfall` in this app demonstrates this
directly — the "unsafe" path can come back with **fewer** elements than
requested (lost updates), and the exact number can vary between runs. The
fix is never "make the loop body synchronized" — it's to let `collect(Collectors.toList())`
do the aggregation, since collectors are specifically designed to merge
per-thread partial results safely.

## 9. Common pitfalls and best practices, gathered in one place

- **A stream can only be consumed once.** Get a fresh one from the source for
  each new pipeline.
- **Don't rely on `peek` for real side effects** — it's a debugging tool; its
  execution isn't guaranteed if the JIT can optimize around it.
- **Prefer `collect` over `forEach` + external mutation** whenever you're
  parallelizing, and prefer it even sequentially for clarity — it keeps the
  accumulation logic in one well-tested place (`Collectors`) instead of a
  hand-rolled side effect.
- **Infinite streams (`generate`/`iterate`) must always be paired with
  `limit`** — forgetting it means the pipeline never terminates.
- **`IntStream`/`LongStream`/`DoubleStream` avoid autoboxing** for numeric
  work — prefer them over `Stream<Integer>` etc. when doing bulk arithmetic;
  `.boxed()` converts back to the object stream only when you actually need
  `Integer`/`Long`/`Double` objects (e.g. to put in a `List<Integer>`).
- **Order of operations can matter for performance**: `filter` before
  `sorted` (shrink the data before doing the expensive full-stream sort) is
  usually better than `sorted` before `filter`.
- **`toMap` without a merge function throws on any duplicate key** — this is
  usually a sign your key function isn't as unique as you assumed; either fix
  the key or supply a merge function on purpose.
- **`Comparator.comparing(...).thenComparing(...)`** replaces multi-field
  hand-written `compareTo` implementations — always reach for this before
  writing a manual comparator.

## 10. Post-Java 8 additions — and why this app leaves them out everywhere else

**Why restrict the rest of the app to Java 8-era methods at all?** Two reasons:

1. **Pedagogical isolation.** `Stream`/`Collectors` grew steadily across Java
   9–16. Mixing eras together in one example makes it impossible to answer
   "was this always possible, or is it new?" without checking the Javadoc
   `@since` tag for every single method. Restricting the core of this app to
   exactly what shipped in Java 8 (2014) gives you a stable, complete
   baseline — the one every "modern Java" comparison article implicitly
   assumes you already know.
2. **It mirrors real interview and real-codebase reality.** Interviewers
   frequently ask "what's the Java 8 way to do X" specifically to test
   whether you know the *foundational* API, not the newest convenience
   method. Plenty of production codebases are still pinned to Java 8/11 and
   genuinely don't have `toList()`, `takeWhile`, or `teeing` available — so
   knowing the Java-8-only equivalent (`collect(Collectors.toList())`,
   `filter` with sorted data, two collectors + two passes) isn't wasted
   knowledge, it's the fallback you actually need there.

That said, the newer additions are genuinely useful and worth knowing —
which is why they live here, in their own controller/service
(`/streams/modern/*`, `ModernStreamOperationsService`), instead of being
silently missing from the app entirely. Each one below is paired with the
Java 8-era method it improves on or replaces:

| Addition                                 | Since | Java 8-era equivalent it replaces/complements                                   |
|------------------------------------------|-------|---------------------------------------------------------------------------------|
| `Stream.toList()`                        | 16    | `collect(Collectors.toList())` — shorter, but returns an **unmodifiable** list. |
| `Stream.takeWhile(Predicate)`            | 9     | `filter` — but stops at the first non-match instead of checking every element.  |
| `Stream.dropWhile(Predicate)`            | 9     | the mirror image of `takeWhile`; no direct Java 8 one-liner equivalent.         |
| `Collectors.teeing(d1, d2, merger)`      | 12    | two separate stream traversals, or one bulky `summarizingDouble`.               |
| `Stream.ofNullable(value)`               | 9     | `value == null ? Stream.empty() : Stream.of(value)`, written out by hand.       |
| `Optional.stream()`                      | 9     | `optional.isPresent() ? Stream.of(optional.get()) : Stream.empty()`.            |
| `Stream.mapMulti(BiConsumer)`            | 16    | `flatMap` — but avoids allocating an intermediate Stream per element.           |
| `Collectors.filtering(pred, downstream)` | 9     | filtering **before** `groupingBy` — which silently loses empty group keys.      |
| `Stream.iterate(seed, hasNext, next)`    | 9     | `Stream.iterate(seed, next).limit(n)` — the 2-arg form is always infinite.      |

**Read `ModernStreamOperationsService`/`ModernStreamOperationsController`
side by side with the Java 8 equivalents** (`IntermediateOperationsService`
for `flatMap` vs `mapMulti`, `CollectorsService` for `groupingBy` vs
`filtering`) — the contrast is the point.

## Interview quick-hits

- **"What's the difference between intermediate and terminal operations?"** —
  Intermediate operations (filter, map, ...) are lazy and return a new
  Stream; nothing executes until a terminal operation (collect, forEach,
  reduce, ...) is called, which triggers the whole pipeline to run in one pass.
- **"Is a stream a data structure?"** — No. It holds no elements; it's a lazy
  description of a computation over some source, and can only be consumed once.
- **"What does short-circuiting mean, and which operations do it?"** — The
  ability to stop processing before visiting every element because the
  answer is already known. `anyMatch`, `allMatch`, `noneMatch`, `findFirst`,
  `findAny`, and `limit` all short-circuit; `count`, `forEach`, and a plain
  `collect` do not.
- **"When would you use `flatMap` instead of `map`?"** — When mapping each
  element would itself produce a stream/collection, and you want one flat
  sequence of the combined results instead of a stream of streams.
- **"Why does `toMap` sometimes throw `IllegalStateException`?"** — Duplicate
  keys with no merge function supplied — the two-argument form doesn't know
  how to resolve the collision.
- **"What's the difference between `groupingBy` and `partitioningBy`?"** —
  `partitioningBy` is a special case of `groupingBy` for a boolean predicate;
  it always produces exactly two keys (`true`/`false`), even if one side is
  empty, whereas `groupingBy` produces one key per distinct classifier value.
- **"Why do `IntStream`/`LongStream`/`DoubleStream` exist at all, instead of
  just using `Stream<Integer>` everywhere?"** — To avoid autoboxing every
  primitive value into its wrapper object, and to gain numeric-only terminal
  operations (`sum`, `average`, `summaryStatistics`) that a generic
  `Stream<T>` has no way to provide.
- **"What does `mapToInt(...).average()` return, and why not just
  `double`?"** — `OptionalDouble`, a primitive specialization of `Optional`,
  because an empty stream has no average to report — the same reasoning
  behind `Stream.min`/`max` returning `Optional<T>`.
- **"Why does the third `reduce` overload need a combiner if a sequential
  stream never calls it?"** — Because the *same* pipeline might run in
  parallel; the combiner is what merges partial results from different
  threads/chunks, and it must be correct even though it's a no-op path on a
  sequential stream.
- **"When do parallel streams actually help?"** — CPU-bound work over a large,
  cheaply-splittable source. They do **not** help I/O-bound work (that's what
  virtual threads/reactive programming are for) and can actively hurt small
  or hard-to-split workloads due to splitting/merging overhead.
- **"What's a common concurrency bug with parallel streams?"** — Mutating a
  shared, non-thread-safe collection (e.g. `ArrayList::add`) from inside a
  parallel `forEach` — a data race. The fix is to use a proper collector
  (`collect(Collectors.toList())`) instead of manual accumulation.
- **"Name a few Stream/Collectors additions after Java 8."** —
  `Stream.toList()` (16), `takeWhile`/`dropWhile` (9), `Collectors.teeing`
  (12), `Stream.ofNullable`/`Optional.stream()` (9), `Stream.mapMulti` (16),
  `Collectors.filtering` (9), and the 3-arg `Stream.iterate` overload (9).
  See `/streams/modern/*` in this app.
- **"Why would this app deliberately avoid using `Stream.toList()`
  everywhere else?"** — To keep the rest of the app a clean, complete
  baseline of exactly what Java 8 offered, so it's unambiguous which
  operations are foundational vs. later convenience additions — and because
  plenty of real Java 8/11-pinned codebases genuinely don't have the newer
  methods available.