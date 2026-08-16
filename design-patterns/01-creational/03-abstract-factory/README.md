# 03 · Abstract Factory

**Intent (one line):** create *families* of related objects without naming their concrete classes — pick the "kit" once, get a consistent set.

## 💡 In plain English
Sometimes objects only make sense **together**: a Light-theme button belongs with a Light-theme checkbox. 
Abstract Factory groups the creation of a whole family behind one factory interface. 
You choose the factory once (Light or Dark), and every object it produces is guaranteed to match — you *can't* accidentally mix a Light button with a Dark checkbox.

## 🎯 Analogy
An **IKEA furniture set**. Buy the "Nordic" kit and every piece matches. You don't hand-pick a Nordic table leg and a Victorian tabletop.

## 🧩 How it works (the moving parts)
- **Abstract factory** (`UIFactory`) → declares `createButton()` + `createCheckbox()`.
- **Concrete factories** (`LightFactory`, `DarkFactory`) → each builds one matching family.
- **Product interfaces** (`Button`, `Checkbox`) → what the client uses.

In the demo, `renderScreen(UIFactory ui)` depends only on the abstract factory. Pass `LightFactory` → matching Light widgets; pass `DarkFactory` → matching Dark widgets. The client never sees a concrete widget class.

## 😖 The pain → ✅ The fix
Hand-`new`-ing individual parts lets incompatible variants mix. The fix: one factory per family builds every member together.

## 🌱 Where you've seen it (Java / Spring)
- JDBC: a `Connection` produces matching `Statement` / `PreparedStatement` objects.
- JPA `EntityManagerFactory`, `DocumentBuilderFactory`, `TransformerFactory`.

## ⚠️ Watch out
- Adding a **new product** to the family (say, a `Slider`) means changing the factory interface **and every** concrete factory. 
- Great for stable families, painful for growing ones.

## 🎤 Interview quick-hits
- **Factory Method vs Abstract Factory?** → Factory Method makes **one** product; Abstract Factory makes a **matching set** (a factory *of* factories).
- **When would you use it?** → cross-platform UI kits, per-database object sets, swappable "themes/kits" where consistency matters.

## ▶️ Run
```bash
java AbstractFactoryDemo.java
```