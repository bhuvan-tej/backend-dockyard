# API Guide — Java 12-17 Features

All endpoints live under the `/api` context path. Base URL used below:
`http://localhost:8080/api`.

Every endpoint returns a `Java17DemoResponse`:

```json
{
  "operation": "what this endpoint demonstrates",
  "description": "plain-English explanation of why it works this way",
  "codeSnippet": "the exact Java that produced the result",
  "result": { }
}
```

---

## 1. Records (`/java17/records`)

### `GET /java17/records/generated-members`
```bash
curl "http://localhost:8080/api/java17/records/generated-members"
```
Reads back, via `Class.getRecordComponents()`, everything the compiler
generated for `record Point(int x, int y)` — accessors (`x()`, **not**
`getX()`), value-based `equals`/`hashCode`, `toString`, the implicit `final`
modifier and the `java.lang.Record` superclass.

### `GET /java17/records/compact-constructor?x=3&y=4`
```bash
curl "http://localhost:8080/api/java17/records/compact-constructor?x=3&y=4"
curl "http://localhost:8080/api/java17/records/compact-constructor?x=-9&y=1"   # rejected
```
| Param | Default | Notes |
|---|---|---|
| `x` | `3` | negative values are rejected by the invariant |
| `y` | `4` | negative values are rejected by the invariant |

A second attempt with `(-1, 5)` is always included so you can see the
invariant fire.

### `GET /java17/records/shallow-immutability`
```bash
curl "http://localhost:8080/api/java17/records/shallow-immutability"
```
Mutates the caller's original list *after* handing it to a record, proving
the defensive copy in the compact constructor is what keeps the record
immutable.

### `GET /java17/records/local-record?csv=spring,java,records`
```bash
curl "http://localhost:8080/api/java17/records/local-record?csv=spring,java,records"
```
A record declared **inside a method**, used as a throwaway tuple in a stream
pipeline.

### `GET /java17/records/limitations`
```bash
curl "http://localhost:8080/api/java17/records/limitations"
```
Documents (as text, since these are compile errors) everything a record
cannot do.

---

## 2. Sealed types (`/java17/sealed`)

### `GET /java17/sealed/shape-hierarchy`
```bash
curl "http://localhost:8080/api/java17/sealed/shape-hierarchy"
```
`Class.getPermittedSubclasses()` on a sealed **interface** — the closed set
is recorded in the class file, so it can be read back at runtime.

### `GET /java17/sealed/vehicle-hierarchy`
```bash
curl "http://localhost:8080/api/java17/sealed/vehicle-hierarchy"
```
The `final` / `sealed` / `non-sealed` rule on a sealed **abstract class** —
each permitted subtype is forced to declare exactly one.

### `GET /java17/sealed/exhaustive-switch`
```bash
curl "http://localhost:8080/api/java17/sealed/exhaustive-switch"
```
A `switch` over all three shapes with **no `default` branch** — accepted
only because the hierarchy is sealed.

### `GET /java17/sealed/pitfalls`
```bash
curl "http://localhost:8080/api/java17/sealed/pitfalls"
```
Module/package scope, direct-subtype requirement, contextual keywords.

---

## 3. Pattern matching & switch expressions (`/java17/pattern-matching`)

### `GET /java17/pattern-matching/instanceof`
```bash
curl "http://localhost:8080/api/java17/pattern-matching/instanceof"
```
Test, cast and bind in one expression, across five different input types.

### `GET /java17/pattern-matching/flow-scoping?value=dockyard`
```bash
curl "http://localhost:8080/api/java17/pattern-matching/flow-scoping?value=dockyard"   # String branch
curl "http://localhost:8080/api/java17/pattern-matching/flow-scoping?value=42"         # non-String branch
```
A purely numeric `value` is boxed to an `Integer` first, so both sides of the
negated guard clause are reachable.

### `GET /java17/pattern-matching/switch-expression?day=saturday`
```bash
curl "http://localhost:8080/api/java17/pattern-matching/switch-expression?day=saturday"
```
Arrow labels, multiple constants per branch, no `break`.

### `GET /java17/pattern-matching/yield?month=5`
```bash
curl "http://localhost:8080/api/java17/pattern-matching/yield?month=5"
curl "http://localhost:8080/api/java17/pattern-matching/yield?month=13"   # hits the default block
```
| Param | Default | Range |
|---|---|---|
| `month` | `5` | -5 – 20 (values outside 1–12 hit the `yield` block) |

### `GET /java17/pattern-matching/sealed-dispatch`
```bash
curl "http://localhost:8080/api/java17/pattern-matching/sealed-dispatch"
```
Pattern matching **plus** a sealed hierarchy — exhaustive, `default`-free
dispatch. (Type patterns in `switch`: preview in 17, standard in 21.)

### `GET /java17/pattern-matching/pitfalls`
```bash
curl "http://localhost:8080/api/java17/pattern-matching/pitfalls"
```

---

## 4. Text blocks (`/java17/text-blocks`)

### `GET /java17/text-blocks/incidental-whitespace`
```bash
curl "http://localhost:8080/api/java17/text-blocks/incidental-whitespace"
```
Three blocks side by side, with `length` and `lineCount` proving what the
compiler stripped. The third is a plain pre-Java-15 concatenation, shown to
be `equals` to the text block.

### `GET /java17/text-blocks/escapes`
```bash
curl "http://localhost:8080/api/java17/text-blocks/escapes"
```
`\` (suppress the line break — `lineCount` drops to 1) and `\s` (a space
that survives trailing-whitespace stripping), plus quote handling.

### `GET /java17/text-blocks/formatted?name=Bhuvan&count=3`
```bash
curl "http://localhost:8080/api/java17/text-blocks/formatted?name=Bhuvan&count=3"
```
| Param | Default | Range |
|---|---|---|
| `name` | `Bhuvan` | any |
| `count` | `3` | 0–999 |

### `GET /java17/text-blocks/pitfalls`
```bash
curl "http://localhost:8080/api/java17/text-blocks/pitfalls"
```

---

## 5. Stream / Collector / String additions (`/java17/api`)

### `GET /java17/api/stream-to-list`
```bash
curl "http://localhost:8080/api/java17/api/stream-to-list"
```
Proves at runtime that `Stream.toList()` is unmodifiable while
`Collectors.toList()` is not — and that `toList()` still tolerates nulls.

### `GET /java17/api/teeing?values=1,2,3,4`
```bash
curl "http://localhost:8080/api/java17/api/teeing?values=1,2,3,4"
curl "http://localhost:8080/api/java17/api/teeing"        # default sample data
```
| Param | Default | Notes |
|---|---|---|
| `values` | `4,8,15,16,23,42` | CSV of numbers |

### `GET /java17/api/string-helpers?input=dockyard`
```bash
curl "http://localhost:8080/api/java17/api/string-helpers?input=dockyard"
```
`formatted` (15), `indent` (12) and `transform` (12), each next to the older
idiom it replaces.

### `GET /java17/api/pitfalls`
```bash
curl "http://localhost:8080/api/java17/api/pitfalls"
```

---

## 6. Runtime / JDK additions (`/java17/jdk`)

### `GET /java17/jdk/helpful-npe`
```bash
curl "http://localhost:8080/api/java17/jdk/helpful-npe"
```
Three NPEs triggered on purpose (a chained map lookup, a nested one, and an
array store) so you can read the JVM's own reconstructed messages, e.g.
`Cannot invoke "String.toUpperCase()" because the return value of
"java.util.Map.get(Object)" is null`.

### `GET /java17/jdk/random-generator?algorithm=L64X128MixRandom&seed=42&count=5`
```bash
curl "http://localhost:8080/api/java17/jdk/random-generator?algorithm=L64X128MixRandom&seed=42&count=5"
curl "http://localhost:8080/api/java17/jdk/random-generator?algorithm=Xoshiro256PlusPlus&seed=7&count=3"
```
| Param | Default | Range |
|---|---|---|
| `algorithm` | `L64X128MixRandom` | any name from `availableAlgorithms` in the response |
| `seed` | `42` | any `long` — same seed always gives the same sequence |
| `count` | `5` | 1–50 |

An unknown algorithm returns `400` with the list of valid names.

### `GET /java17/jdk/compact-numbers`
```bash
curl "http://localhost:8080/api/java17/jdk/compact-numbers"
```
SHORT (`1M`), LONG (`1 million`) and plain (`1,000,000`) forms side by side.

### `GET /java17/jdk/pitfalls`
```bash
curl "http://localhost:8080/api/java17/jdk/pitfalls"
```