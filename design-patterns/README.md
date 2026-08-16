# 🧩 Design Patterns in Java

> Reusable solutions to problems that show up again and again in real code.
> This folder is **curated, not exhaustive**: the Gang of Four (GoF) defined 23
> patterns, but these **15** cover the vast majority of interview questions and
> real Spring Boot / backend code you'll actually write.

> Same recipe as the rest of this repo: **problem → naïve code → pattern → when NOT to use it**.
> A pattern you can't *justify* is just extra indirection.

---

## 🎯 How This Folder Works

Patterns are grouped into the three GoF categories. Each pattern lives in its own
numbered folder under its category and always contains:

| File        | Purpose                                                                   |
|-------------|---------------------------------------------------------------------------|
| `README.md` | The problem it solves, the structure, a real Java/Spring example, gotchas |
| `*.java`    | Runnable code — the *painful* version first, then the pattern applied     |

> 🎤 **Prepping for interviews?** See **[`INTERVIEW_QUESTIONS.md`](./INTERVIEW_QUESTIONS.md)** —
> general concepts, one Q&A block per pattern, and a "which pattern?" scenario table.

**The golden rule of each pattern:**
1. **Feel the pain first** — write the rigid/duplicated code the pattern fixes.
2. **Apply the pattern** — show how it removes the pain.
3. **Know the trade-off** — every pattern adds indirection; know when it's *not* worth it.

---

## 🗺️ The 15 Patterns (learn in this order)

### 🏗️ Creational — *how objects get made*

| #  | Pattern              | One-line intent                                              | You've seen it in…                                         |
|----|----------------------|--------------------------------------------------------------|------------------------------------------------------------|
| 01 | **Singleton**        | Exactly one instance, shared globally.                       | Spring `@Bean`s (singleton scope), `Runtime`               |
| 02 | **Factory Method**   | Let a method decide *which* subclass to instantiate.         | `Calendar.getInstance()`, `BeanFactory`                    |
| 03 | **Abstract Factory** | Create *families* of related objects without naming classes. | JDBC `Connection`, JPA `EntityManagerFactory`              |
| 04 | **Builder**          | Build a complex object step-by-step, immutably.              | Lombok `@Builder`, `StringBuilder`, `UriComponentsBuilder` |

### 🧱 Structural — *how objects are composed*

| #  | Pattern       | One-line intent                                             | You've seen it in…                                       |
|----|---------------|-------------------------------------------------------------|----------------------------------------------------------|
| 05 | **Adapter**   | Make an incompatible interface fit what the client expects. | `InputStreamReader`, `HandlerAdapter`                    |
| 06 | **Decorator** | Add behavior by wrapping, not subclassing.                  | `BufferedReader`, Spring `HttpMessageConverter` wrapping |
| 07 | **Facade**    | One simple entry point over a messy subsystem.              | Spring `JdbcTemplate`, `@Service` layers                 |
| 08 | **Proxy**     | A stand-in that controls access (lazy, security, caching).  | Spring AOP, `@Transactional`, JPA lazy loading           |
| 09 | **Composite** | Treat single objects and groups of objects uniformly.       | UI trees, file systems, `CompositeHealthContributor`     |

### 🔁 Behavioral — *how objects talk and share responsibility*

| #  | Pattern             | One-line intent                                    | You've seen it in…                                |
|----|---------------------|----------------------------------------------------|---------------------------------------------------|
| 10 | **Strategy**        | Swap an algorithm at runtime behind one interface. | `Comparator`, Spring `PasswordEncoder`            |
| 11 | **Observer**        | Notify many subscribers when state changes.        | Spring `ApplicationEvent` / listeners             |
| 12 | **Command**         | Wrap a request as an object (queue, undo, log it). | `Runnable`, `Callable`, job queues                |
| 13 | **Template Method** | Fix the skeleton, let subclasses fill the blanks.  | `JdbcTemplate`, `AbstractList`, servlet lifecycle |
| 14 | **State**           | Change behavior when internal state changes.       | Order/workflow state machines, TCP sockets        |
| 15 | **Iterator**        | Walk a collection without exposing its internals.  | `Iterator`, the `for-each` loop                   |

> 💡 **Why these 15?** They're the ones interviewers actually ask about *and* the
> ones baked into frameworks you use daily. Master Strategy, Factory, Builder,
> Decorator, Proxy, Observer and Template Method first — Spring is built out of them.

---

## 🧭 The one mental model

Almost every pattern is a variation of one idea: **"program to an interface, not an
implementation"** + **"favor composition over inheritance."**

```
Creational  →  hides HOW an object is created      (new is the enemy of flexibility)
Structural  →  hides HOW objects are wired together (wrap / compose instead of edit)
Behavioral  →  hides HOW responsibility is shared   (delegate instead of if/else trees)
```

If a chain of `if/else` or `switch` on a *type* is growing → you probably want
**Strategy**, **State**, or a **Factory**. If you're editing a class every time a
new variant appears → you're missing an abstraction.

---

## 🗂️ Folder layout

```
design-patterns/
├── 01-creational/
│   ├── 01-singleton/
│   ├── 02-factory-method/
│   ├── 03-abstract-factory/
│   └── 04-builder/
├── 02-structural/
│   ├── 05-adapter/
│   ├── 06-decorator/
│   ├── 07-facade/
│   ├── 08-proxy/
│   └── 09-composite/
└── 03-behavioral/
    ├── 10-strategy/
    ├── 11-observer/
    ├── 12-command/
    ├── 13-template-method/
    ├── 14-state/
    └── 15-iterator/
```

Like `dsa/`, this is **plain Java** — no build tool, one runnable `public class`
per file, each with a `main` that shows the pattern in action.

---

## ⚠️ The anti-pattern warning

Design patterns are a **vocabulary**, not a checklist. Do not force a pattern in
just to use it — over-engineering is a worse smell than a bit of duplication.
Reach for a pattern only when the pain (rigid code, duplication, a growing
`switch`) is real.

---

Start with **[01 — Singleton »](./01-creational/01-singleton/)**.