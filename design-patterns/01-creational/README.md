# 🏗️ Creational Patterns

> **Concern:** *how objects get created.* The enemy is the raw `new` keyword
> scattered everywhere — it hard-wires your code to concrete classes and makes
> construction logic impossible to reuse or swap.

| #  | Pattern                                        | Intent                                    | When to reach for it                                  |
|----|------------------------------------------------|-------------------------------------------|-------------------------------------------------------|
| 01 | **[Singleton](./01-singleton/)**               | One shared instance for the whole app.    | Config, caches, connection pools, Spring beans        |
| 02 | **[Factory Method](./02-factory-method/)**     | A method decides which subclass to build. | Creation depends on input/config, not hard-coded      |
| 03 | **[Abstract Factory](./03-abstract-factory/)** | Build whole families of related objects.  | Swappable "kits" (e.g. per-DB, per-OS, per-theme)     |
| 04 | **[Builder](./04-builder/)**                   | Assemble a complex object step-by-step.   | Many optional fields; want immutability + readability |

**Tip:** if a constructor has 5+ params or lots of optional ones → **Builder**.
If you keep writing `new SomethingConcrete()` and it changes by context → **Factory**.