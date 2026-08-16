# 01 · Singleton

**Intent (one line):** guarantee a class has exactly **one** instance and give one global access point to it.

## 💡 In plain English
Some things should exist only once in an app — a configuration holder, a connection pool, a logger. Singleton makes the class itself responsible for enforcing 
"there's only ever one of me," and hands that same instance to everyone. 
You make the constructor **private** (so nobody outside can call `new`) and expose a static `getInstance()` that always returns the one cached object.

## 🎯 Analogy
The **President of a country** — exactly one at a time, and everyone refers to "the President." You don't create your own.

## 🧩 How it works (the moving parts)
- **Private constructor** → blocks `new AppConfig()` from anywhere else.
- **A single cached instance** → created once, stored in a static field.
- **`getInstance()`** → the only way in; always returns that same object.

In the demo, `NaiveConfig` is created twice and prints "loaded from disk" **twice** (wasteful). `AppConfig` uses the **initialization-on-demand holder idiom** — 
the nested `Holder` class isn't loaded until `getInstance()` is first called, so the instance is built **lazily and thread-safely** with no `synchronized`. 
Both calls return the same object (`a == b` → `true`).

## 😖 The pain → ✅ The fix
`new AppConfig()` everywhere re-runs the expensive load and gives no single source of truth. The fix: one private-constructed instance handed out on demand.

## 🌱 Where you've seen it (Java / Spring)
- Spring `@Bean`s are **singleton-scoped by default** (Spring owns the one instance for you).
- `java.lang.Runtime.getRuntime()`, a shared `Logger`, connection pools.

## ⚠️ Watch out
- It's **global mutable state** in disguise → hard to unit-test and to parallelize.
- Naïve lazy singletons are **not thread-safe**. Use the **holder idiom** (this file) or an **`enum`** (simplest, serialization-safe).
- Prefer **dependency injection** (let Spring hold the single instance) over hand-rolled singletons.

## 🎤 Interview quick-hits
- **How do you make it thread-safe?** → holder idiom, `enum`, or double-checked locking with a `volatile` field.
- **Why is `enum` the "best" singleton?** → the JVM guarantees a single instance and it's safe against reflection and serialization attacks.
- **Why do people call it an anti-pattern?** → hidden global state, tight coupling, testability pain.

## ▶️ Run
```bash
java SingletonDemo.java
```