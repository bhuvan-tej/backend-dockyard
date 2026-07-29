# 🌱 Spring Boot Notes

> Hands-on, interview-ready notes for the Spring Boot projects in this repo.
> Every annotation is explained in plain English, with **what it does**, **why it exists**,
> and a **real example** taken from the projects here (`01-rest-api-crud` → `07-ai-rag-service`).

---

## 📚 How to use these notes

Read them top to bottom the first time. After that, treat them as a **reference** —
each file is self-contained and grouped by topic. Every annotation has a one-line
"cheat" summary plus a fuller explanation.

Legend used throughout:

- 🧩 **What** – what the annotation/concept does
- 🎯 **Why** – the problem it solves
- 🧪 **Example** – code from this repo
- ⚠️ **Gotcha** – common mistake or interview trap

---

## 🗂️ Table of Contents

| #  | Topic                                                              | You'll learn                                                     |
|----|--------------------------------------------------------------------|------------------------------------------------------------------|
| 01 | [Spring Boot Fundamentals](./01-spring-boot-fundamentals.md)       | IoC, DI, auto-configuration, app lifecycle, layered architecture |
| 02 | [Annotations Master Reference](./02-annotations-reference.md)      | **Every annotation** used in this repo, grouped and explained    |
| 03 | [REST & Web Layer](./03-rest-api-web.md)                           | Controllers, mappings, request/response, status codes            |
| 04 | [Data & JPA](./04-data-jpa.md)                                     | Entities, repositories, queries, transactions                    |
| 05 | [Bean Validation](./05-validation.md)                              | `@Valid`, constraints, custom messages                           |
| 06 | [Exception Handling](./06-exception-handling.md)                   | `@RestControllerAdvice`, consistent error responses              |
| 07 | [Dependency Injection & Beans](./07-dependency-injection-beans.md) | Stereotypes, `@Bean`, `@Configuration`, wiring                   |
| 08 | [Lombok](./08-lombok.md)                                           | Boilerplate removal — `@Data`, `@Builder`, `@Slf4j`…             |
| 09 | [Security & JWT](./09-security-jwt.md)                             | Auth flow, OTP, JWT, refresh tokens                              |
| 10 | [Testing](./10-testing.md)                                         | Unit vs integration, mocking, slice tests                        |
| 11 | [Configuration & Properties](./11-configuration-properties.md)     | `application.yml`, `@ConfigurationProperties`, profiles          |
| 12 | [Spring AI](./12-spring-ai.md)                                     | ChatClient, embeddings, RAG basics                               |
| ⚡ | [Interview Quick Sheet](./13-interview-quick-sheet.md)             | Rapid-fire Q&A for revision                                      |

---

## 🧭 Mental model of a Spring Boot request

```
        HTTP Request
             │
             ▼
   ┌──────────────────┐   @RestController / @GetMapping / @PostMapping
   │   Controller     │   validates input (@Valid), returns ResponseEntity
   └──────────────────┘
             │  DTOs (@Data / @Builder)
             ▼
   ┌──────────────────┐   @Service — business logic, @Transactional
   │    Service        │   maps DTO ↔ Entity, throws domain exceptions
   └──────────────────┘
             │
             ▼
   ┌──────────────────┐   @Repository — Spring Data JPA
   │   Repository     │   generates SQL from method names / @Query
   └──────────────────┘
             │
             ▼
        Database (@Entity → table)

   Cross-cutting: @RestControllerAdvice (errors), @Configuration/@Bean (wiring),
                  @ConfigurationProperties (config), Security filter chain
```

---

## 🏭 The projects these notes are based on

| Project               | Focus         | Key topics                                                 |
|-----------------------|---------------|------------------------------------------------------------|
| `01-rest-api-crud`    | CRUD REST API | DTOs, validation, pagination, exception handling           |
| `02-qr-generator`     | Utility API   | Services, binary responses                                 |
| `03-otp-jwt-auth`     | Auth          | Spring Security, JWT, OTP, refresh rotation                |
| `04-spring-ai-basics` | AI            | Spring AI ChatClient, structured output                    |
| `05-semantic-search`  | AI            | Embeddings, vector store, semantic search (the "R" of RAG) |
| `06-tool-calling`     | AI            | Tool/function calling — the LLM calls your Java methods    |
| `07-ai-rag-service`   | AI + data     | Embeddings, vector store, RAG pipeline                     |

Happy learning! Start with **[01 – Fundamentals](./01-spring-boot-fundamentals.md)**.