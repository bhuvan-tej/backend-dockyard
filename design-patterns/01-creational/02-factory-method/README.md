# 02 · Factory Method

**Intent (one line):** let a dedicated method decide *which* concrete class to instantiate, so callers depend on an interface instead of `new`.

## 💡 In plain English
Whenever you write `new SomethingConcrete()`, your code is now welded to that exact class. 
Factory Method moves that decision into one place — a factory method — that returns the abstract type. 
Callers say "give me a `Notifier`" and never learn (or care) which concrete class they got. 
Adding a new variant touches only the factory, not the callers.

## 🎯 Analogy
Ordering "a coffee" at a café. You ask the **barista** (the factory); they decide whether to build an espresso or a latte. You just get a `Coffee`.

## 🧩 How it works (the moving parts)
- **Product interface** (`Notifier`) → what callers depend on.
- **Concrete products** (`EmailNotifier`, `SmsNotifier`, `PushNotifier`) → the variants.
- **Factory method** (`create(channel)`) → owns the `switch` and returns the interface.

In the demo, the loop asks `create("EMAIL"/"SMS"/"PUSH")` and calls `send(...)` — never naming a concrete class. 
To add a "Slack" channel, you add one class and one `case`; **no caller changes.**

## 😖 The pain → ✅ The fix
`new EmailNotifier()` and `if (type == "SMS")` scattered across callers → adding a channel edits every caller. The fix: one factory owns creation.

## 🌱 Where you've seen it (Java / Spring)
- `Calendar.getInstance()`, `NumberFormat.getInstance()`, `LoggerFactory.getLogger(...)`.
- Spring `BeanFactory` / `ApplicationContext.getBean(...)`.

## ⚠️ Watch out
- The classic GoF "Factory **Method**" is an *overridable method on a subclass*; the single static `create(...)` here is often called a "Simple Factory." 
- Same intent, and interviewers accept both — just know the distinction.
- If you need whole **families** of related objects → step up to **Abstract Factory**.

## 🎤 Interview quick-hits
- **Factory Method vs Simple Factory?** → Simple Factory is one static method with a `switch`; GoF Factory Method defers creation to subclasses via an overridable method.
- **Why bother instead of `new`?** → decouples callers from concrete classes; open/closed — extend without editing callers.

## ▶️ Run
```bash
java FactoryMethodDemo.java
```