# 🌱 Spring AI Basics

## 🎯 Goal

---
The **cleanest possible introduction to Spring AI** — nothing but the essentials of
talking to a Large Language Model (LLM) the "Spring way".

No database. No vector store. **No RAG.** Just one `ChatClient` and the five things
you'll do 90% of the time when building AI features on the JVM:

| # | Endpoint               | Spring AI idea it teaches                                     |
|---|------------------------|---------------------------------------------------------------|
| 1 | `POST /chat/ask`       | **Ask** — text in, text out (the "hello world")               |
| 2 | `POST /chat/persona`   | **System prompt** — set the model's behaviour/persona         |
| 3 | `POST /chat/translate` | **Prompt template** — a reusable prompt with `{placeholders}` |
| 4 | `GET  /chat/country`   | **Structured output** — get a typed Java object, not a String |
| 5 | `GET  /chat/stream`    | **Streaming** — the answer arrives token-by-token (SSE)       |

> 🧭 This is the **foundation**
> Understand these five patterns here first.

## 🤔 What is Spring AI?

---
**Spring AI** is Spring's official framework for talking to AI models (LLMs,
embedding models, image models…) with auto-configuration, dependency injection and
a clean, **portable** API.

Before Spring AI you'd hand-roll HTTP calls to each provider (OpenAI, Ollama,
Anthropic…), each with a different JSON shape. Spring AI gives you **one interface**
— `ChatClient` — and swaps the provider behind it via configuration.

```
Your code ──► ChatClient (Spring AI) ──► Ollama  (local, default)
                                     └─► OpenAI / Anthropic / Azure … (swap in yaml)
```

Think of `ChatClient` as the `RestClient` / `JdbcTemplate` of the AI world.

## 🏗️ Architecture

---
Same layered, "isolate the third-party engine" design as every other project in the repo:

```
HTTP Request
      │
      ▼
ChatController          thin — validates input, shapes the HTTP response.
      │                 imports NO org.springframework.ai.*
      ▼
AssistantEngine ──────► ★ the ONLY class that imports org.springframework.ai.*
      │                   the five patterns, one method each
      ▼
   ChatClient  (Spring AI)  ──►  Ollama (qwen2.5, local & free)
```

```
spring-ai-basics/
├── src/main/java/com/dockyard/springai/
│   ├── SpringAiBasicsApplication.java   entry point
│   ├── ai/                              ★ the ONLY package importing Spring AI
│   │   ├── AiBeans.java                 builds the ChatClient bean
│   │   └── AssistantEngine.java         ask · askAs · askTemplate · askFor · stream
│   ├── config/OpenApiConfig.java        Swagger UI metadata
│   ├── controller/ChatController.java   the 5 endpoints (1 per pattern)
│   ├── dto/                             request/response records + CountryFacts
│   └── exception/                       503 when the model is down, 400 on bad input
├── src/test/java/com/dockyard/springai/
│   ├── SpringAiBasicsApplicationTests   context smoke test (ChatModel mocked)
│   └── ai/AssistantEngineTest           error-mapping unit test (no network)
├── application.yml                      the ONLY AI config lives here
├── Dockerfile                           multi-stage build → slim non-root JRE
├── docker-compose.yml                   app + Ollama, one command
├── .dockerignore
└── pom.xml
```

**Why isolate Spring AI in `AssistantEngine`?** The controller depends on our own
methods, never on `org.springframework.ai.*`. Swapping Ollama → OpenAI, or the
model, is a **pom + yaml change** — no Java touched. It also lets us translate any
provider failure into one clean `AiUnavailableException` at a single boundary.

## ✅ Running Locally

> Project requires a model provider. The default is **Ollama**,
> which runs models on your own machine for **free — no API key, no cloud bill**.

### Step 1 — install Ollama and pull the chat model (one time)
```bash
# macOS
brew install ollama
ollama serve            # starts the local model server on :11434

# in another terminal, pull the chat model (this project uses ONE model)
ollama pull qwen2.5
```

### Step 2 — run the app
```bash
./mvnw spring-boot:run
```
> The app auto-pulls `qwen2.5` on first startup if it's missing
> (`pull-model-strategy: when_missing`), so Step 1 is optional if Ollama is running.
> `qwen2.5` follows JSON schemas well, which keeps the structured-output demo
> (`/chat/country`) reliable. Prefer a smaller/faster model? Set
> `AI_CHAT_MODEL=llama3.2` (or any Ollama model) — no code change.

Then open **Swagger UI**: <http://localhost:8080/api/swagger-ui.html>

### Option — Docker (app **and** Ollama together, one command)
```bash
docker compose up --build
```
> The provided `Dockerfile` is a multi-stage build (JDK to compile → slim non-root
> JRE to run), and `docker-compose.yml` bundles Ollama, so `app + local LLM` come up
> together with no API key. First run downloads the model inside the Ollama container
> (a couple of GB) — be patient. On Apple silicon, native Ollama (above) is faster
> than the container because the container can't use the GPU.

### Prefer OpenAI instead of local models?
```
1. pom.xml    — comment spring-ai-starter-model-ollama, uncomment ...-openai
2. application.yml — replace the `spring.ai.ollama` block with:
     openai:
       api-key: ${OPENAI_API_KEY}
       chat:
         options:
           model: gpt-4o-mini
3. export OPENAI_API_KEY=sk-...  and run. No Java changes — that's the whole point
   of isolating Spring AI behind AssistantEngine.
```

## 🧪 Trying It Out

### 1. Ask — text in, text out
```bash
curl -X POST http://localhost:8080/api/chat/ask \
  -H "Content-Type: application/json" \
  -d '{"message":"Explain what an API is in one sentence."}'
# → {"answer":"An API is a contract that lets one program request services from another."}
```

### 2. Persona — a system prompt steers the reply
```bash
curl -X POST http://localhost:8080/api/chat/persona \
  -H "Content-Type: application/json" \
  -d '{"persona":"You are a terse pirate. Answer in one short sentence.",
       "message":"How is the weather at sea today?"}'
# → {"answer":"Arr, the winds be fair and the seas be calm, matey."}
```

### 3. Template — a reusable prompt with variables filled in
```bash
curl -X POST http://localhost:8080/api/chat/translate \
  -H "Content-Type: application/json" \
  -d '{"text":"Good morning, friend","language":"French"}'
# → {"answer":"Bonjour, mon ami"}
```

### 4. Structured output — a typed object, not a String
```bash
curl "http://localhost:8080/api/chat/country?name=Japan"
# → {"name":"Japan","capital":"Tokyo","population":125000000,"languages":["Japanese"]}
```

### 5. Streaming — tokens arrive as they're generated (SSE)
```bash
curl -N "http://localhost:8080/api/chat/stream?message=Write%20a%20haiku%20about%20the%20sea"
# → data: Waves...
#   data:  whisper...
#   data:  soft...   (streams in real time)
```

### Bonus — the guardrail when the model is down
```bash
# stop `ollama serve`, then:
curl -X POST http://localhost:8080/api/chat/ask \
  -H "Content-Type: application/json" -d '{"message":"hi"}'
# → 503 {"status":503,"message":"The AI model is unavailable. Is Ollama running..."}
```

## 📋 Endpoints

---
| Method | URL                     | Teaches            | Success | Errors    |
|--------|-------------------------|--------------------|---------|-----------|
| POST   | /api/chat/ask           | simple chat        | 200     | 400 / 503 |
| POST   | /api/chat/persona       | system prompt      | 200     | 400 / 503 |
| POST   | /api/chat/translate     | prompt template    | 200     | 400 / 503 |
| GET    | /api/chat/country       | structured output  | 200     | 503       |
| GET    | /api/chat/stream        | streaming (SSE)    | 200     | 503       |
| GET    | /api/actuator/health    | health check       | 200     | —         |

## 🔑 Key Concepts

---
### The `ChatClient` fluent API
```
chatClient.prompt()      // start a request
    .system("...")       // optional: persona / rules
    .user("...")         // the human message
    .call()              // send + block   (or .stream() for tokens)
    .content();          // the text       (or .entity(Type.class) for objects)
```
Every method in `AssistantEngine` is a tiny variation on this one chain.

### System vs. user prompt
The **system** prompt says *who the model is and how to behave*; the **user** prompt
is *the actual request*. Same user question + different system prompt = different
answer. That's how you build a "concise senior engineer" or a "JSON-only bot".

### Structured output
`.entity(CountryFacts.class)` makes Spring AI append "reply as JSON matching this
schema" to the prompt, then parses the reply into your record. No manual JSON.

> ⚠️ **Structured output can be fragile on tiny models.** A small model like
> llama3.2 (3B) is unreliable at *free-form* JSON: it renames fields, wraps the
> reply in prose/markdown, or simply **stops before the closing `}`**, so parsing
> fails with `JsonEOFException: Unexpected end-of-input`. (With `temperature 0` the
> same broken reply returns every time.) Two things keep this project reliable:
> (1) the default model is **`qwen2.5`**, which follows JSON schemas well; and
> (2) `AssistantEngine.askFor` uses the provider's **native JSON mode**
> (`OllamaOptions.format("json")`), which grammar-constrains Ollama so the reply is
> *always* complete, valid JSON — plus a strict system prompt and `temperature 0`.
> The JSON-mode line is the one place tied to Ollama (it has no portable equivalent
> yet; for OpenAI you'd set its `json_object` response format instead). Want a
> smaller/faster model? `AI_CHAT_MODEL=llama3.2` — no code change.

### Streaming
`.stream().content()` returns a reactive `Flux<String>` instead of one String. The
controller serves it as Server-Sent Events — the "typing" effect in chat UIs.

## 🧰 Tech Stack

---
| Tool                  | Purpose                                        |
|-----------------------|------------------------------------------------|
| Java 21 (LTS)         | Language                                       |
| Spring Boot 3.5       | Web, Validation, Actuator                      |
| Spring AI 1.0         | `ChatClient` (isolated in `ai/`)               |
| Ollama (default)      | Local, free LLM — no API key                   |
| SpringDoc OpenAPI     | Swagger UI                                     |
| Lombok                | Boilerplate reduction                          |
| JUnit 5 + Mockito     | Tests (model mocked — no network)              |
| Docker (multi-stage)  | Slim, non-root runtime image + bundled Ollama  |

## 💡 Interview Questions

---
**Q: What is Spring AI and why use it over calling an LLM API directly?**
> It's Spring's framework for talking to AI models with one portable API
> (`ChatClient`), plus auto-configuration and DI. You depend on the interface, so
> switching provider (Ollama ↔ OpenAI) or model is a config change, not a rewrite —
> and you get retries, observability and structured-output parsing for free.

**Q: Difference between a system prompt and a user prompt?**
> The system prompt sets the model's role and rules ("you are a terse pirate");
> the user prompt is the actual request. The system prompt governs *how* every user
> message is answered.

**Q: How do you get a typed Java object out of an LLM instead of a String?**
> `.call().entity(MyType.class)`. Spring AI injects a JSON-schema instruction into
> the prompt and parses the response into your class — no manual JSON handling.

**Q: What does streaming change?**
> `.stream().content()` returns a `Flux<String>` that emits tokens as they're
> generated, instead of blocking for the whole answer. It powers the real-time
> "typing" effect and lowers time-to-first-token.

**Q: Why wrap `ChatClient` in `AssistantEngine`?**
> Ports-and-adapters. Controllers depend on our methods, not on
> `org.springframework.ai.*`, so provider swaps and error handling live in one
> place and the AI code is trivially mockable in tests.

## 🔗 Where to go next

- **[SPRING_AI_EXPLAINED.md](SPRING_AI_EXPLAINED.md)** — the same ideas with diagrams.