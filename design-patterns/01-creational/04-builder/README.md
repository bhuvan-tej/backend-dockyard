# 04 · Builder

**Intent (one line):** assemble a complex object step-by-step and produce an immutable result.

## 💡 In plain English
When an object has many fields — especially optional ones — constructors get ugly fast (`new Request(url, "GET", null, 30, true, null)`). 
Nobody can tell what each argument means, and you end up with a dozen overloads. 
Builder gives you a small helper object with **named, chainable** setters; you set only what you need, then call `build()` to get a finished, **immutable** object.

## 🎯 Analogy
A **Subway sandwich order**. You start with bread, then chain "add cheese," "add lettuce," skip what you don't want, and finally "make it." 
The sandwich is only "served" once complete.

## 🧩 How it works (the moving parts)
- **The product** (`HttpRequest`) → `final` fields, no setters, private constructor.
- **The builder** → holds required fields (constructor arg) + optionals with **defaults**; each setter returns `this` for chaining; `build()` constructs the product.

In the demo, request `a` sets nothing extra and keeps sane defaults (`GET`, 30s, redirects on). Request `b` chains `.method("POST").timeoutSec(5).followRedirects(false)`. 
Both are readable and immutable once built.

## 😖 The pain → ✅ The fix
Telescoping constructors are unreadable and multiply with every optional field. The fix: fluent, named, defaulted builder → one readable construction path.

## 🌱 Where you've seen it (Java / Spring)
- `StringBuilder`, `Stream.Builder`, `HttpRequest.newBuilder()`, `UriComponentsBuilder`.
- **Lombok `@Builder`** generates exactly this boilerplate for you.

## ⚠️ Watch out
- Overkill for 1–2 fields — just use a constructor.
- Put **validation in `build()`** so you never construct an invalid object.

## 🎤 Interview quick-hits
- **Builder vs telescoping constructors?** → Builder is readable, avoids overload explosion, and enforces immutability.
- **Builder vs setters (JavaBeans)?** → setters leave the object mutable and temporarily in an inconsistent state; Builder produces a fully-formed immutable object at once.
- **Where's it in the JDK?** → `StringBuilder`, `Stream.Builder`, `Calendar.Builder`.

## ▶️ Run
```bash
java BuilderDemo.java
```