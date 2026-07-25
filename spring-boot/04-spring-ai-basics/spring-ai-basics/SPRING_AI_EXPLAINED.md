# 🌱 Spring AI, Explained Cleanly (with diagrams)

This is Spring AI stripped to its core — **one `ChatClient`, five patterns**. Once
these click, everything else in the AI ecosystem (RAG, agents, tools) is just these
same primitives arranged differently.

> The diagrams below are **Mermaid** — they render automatically on GitHub. In a
> plain editor the blocks still read top-to-bottom.

---

## 1. What is Spring AI?

**Spring AI** is Spring's official framework for talking to AI models the "Spring
way" — auto-configuration, dependency injection, and one **portable** API.

Without it, every provider is a different hand-rolled HTTP + JSON integration. With
it, you inject **one interface** and choose the provider in `application.yml`.

```mermaid
flowchart LR
    App["Your code<br/>(ChatController)"] --> CC["ChatClient<br/>(Spring AI interface)"]
    CC --> Ollama["Ollama<br/>(local, default, free)"]
    CC -. swap via config .-> OpenAI["OpenAI"]
    CC -. swap via config .-> Others["Anthropic / Azure / ..."]
```

`ChatClient` is to AI what `JdbcTemplate` is to databases: a clean, high-level
client that hides the provider's wire format.

---

## 2. The one API you must know

Every call is a variation of this fluent chain:

```mermaid
flowchart LR
    P["prompt()"] --> S["system(...)<br/>optional persona/rules"]
    S --> U["user(...)<br/>the human message"]
    U --> CALL["call()  →  block for reply<br/>OR<br/>stream() → tokens"]
    CALL --> OUT["content()  →  String<br/>OR<br/>entity(Type) → object"]
```

That's it. The five endpoints below just pick different branches of this chain.

---

## 3. The five patterns (mapped to real methods)

Every method lives in **`AssistantEngine`** — the only class that imports
`org.springframework.ai.*`.

```mermaid
flowchart TB
    subgraph web["controller/ — thin HTTP (no Spring AI here)"]
        C1["POST /chat/ask"]
        C2["POST /chat/persona"]
        C3["POST /chat/translate"]
        C4["GET /chat/country"]
        C5["GET /chat/stream"]
    end
    subgraph ai["ai/ — ★ the ONLY Spring AI code"]
        M1["ask(msg)"]
        M2["askAs(system, msg)"]
        M3["askTemplate(tmpl, vars)"]
        M4["askFor(msg, Type)"]
        M5["stream(msg)"]
        CLIENT["ChatClient"]
    end
    Ollama["Ollama (qwen2.5)"]

    C1 --> M1
    C2 --> M2
    C3 --> M3
    C4 --> M4
    C5 --> M5
    M1 & M2 & M3 & M4 & M5 --> CLIENT --> Ollama
```

### 3.1 Ask — the "hello world"
`prompt().user(msg).call().content()` — text in, text out.

```mermaid
sequenceDiagram
    autonumber
    actor U as Client
    participant C as ChatController
    participant E as AssistantEngine
    participant L as Ollama (LLM)
    U->>C: POST /chat/ask {message}
    C->>E: ask(message)
    E->>L: prompt().user(message).call()
    L-->>E: "an answer"
    E-->>U: {answer}
```

### 3.2 Persona — a system prompt steers behaviour
The **system** prompt sets *who the model is*; the **user** prompt is *the request*.

```mermaid
flowchart LR
    SYS["system: 'You are a terse pirate.'"] --> LLM
    USR["user: 'How's the weather?'"] --> LLM
    LLM["LLM"] --> ANS["'Arr, the seas be calm, matey.'"]
```

Same user question + different system prompt = different answer. This is the single
most powerful knob you have.

### 3.3 Template — reuse a prompt, vary the inputs
Keep a stable, tested prompt with `{placeholders}`; Spring AI fills them per call.

```
template = "Translate '{text}' into {language}. Reply with ONLY the translation."
params   = { text: "Good morning", language: "French" }
          ─────────────────────────────────────────────►  "Bonjour"
```

### 3.4 Structured output — a typed object, not a String
Ask for a Java type; Spring AI adds "reply as JSON like this schema" and parses it.

```mermaid
flowchart LR
    Q["askFor('facts about Japan', CountryFacts.class)"] --> INJ["Spring AI adds<br/>JSON-schema instruction"]
    INJ --> LLM["LLM replies as JSON"]
    LLM --> P["Spring AI parses"]
    P --> OBJ["CountryFacts{name, capital,<br/>population, languages}"]
```

You never touch JSON — you get a real object.

### 3.5 Streaming — tokens as they're generated
`stream().content()` returns a `Flux<String>`; the controller serves it as
Server-Sent Events (the "typing" effect).

```mermaid
sequenceDiagram
    autonumber
    actor U as Browser
    participant E as AssistantEngine
    participant L as Ollama
    U->>E: GET /chat/stream?message=...
    E->>L: prompt().user(msg).stream()
    L-->>U: "Waves"
    L-->>U: " whisper"
    L-->>U: " soft" (each token as it's ready)
```

---

## 4. The isolation boundary (the "why")

```mermaid
flowchart LR
    subgraph ours["Our code — knows nothing about Spring AI"]
        CTRL["ChatController"]
        DTO["DTOs (AskRequest, CountryFacts...)"]
    end
    subgraph seam["ai/ — the adapter (only Spring AI imports live here)"]
        ENG["AssistantEngine"]
        BEANS["AiBeans"]
    end
    lib["Spring AI · ChatClient · Ollama"]
    ours --> seam --> lib
```

| Decision                              | Why                                                                 |
|---------------------------------------|---------------------------------------------------------------------|
| **Spring AI isolated in `ai/`**       | Controller depends on our methods; provider/model swap = config only |
| **Ollama by default**                 | Free & local — no API key, no cloud bill                            |
| **One `AiUnavailableException`**      | Every provider failure becomes 503 at a single boundary            |
| **`.entity(Type)` for structured out**| No manual JSON parsing; you get a real Java object                 |

This is **ports & adapters** (hexagonal) — the exact move the QR project makes with
ZXing and the OTP project makes with JJWT.

---

## 5. How this grows into RAG

RAG (the [`05-ai-rag-service`](../../05-ai-rag-service) project) is **not** a new
Spring AI feature — it's these primitives plus a search step:

```mermaid
flowchart LR
    Q["question"] --> SEARCH["find relevant docs<br/>(vector store)"]
    SEARCH --> CTX["build a CONTEXT block"]
    CTX --> ASK["askAs(system='use only this context', user=context+question)"]
    ASK --> ANS["grounded answer"]
```

If you understand `askAs` here, RAG is just: *retrieve first, then call it.*

---

## 6. Glossary

| Term                  | Plain-English meaning                                                     |
|-----------------------|--------------------------------------------------------------------------|
| **LLM**               | Large Language Model — the thing that writes the answer (e.g. Llama 3.2)  |
| **ChatClient**        | Spring AI's high-level client for chatting with an LLM                    |
| **System prompt**     | Instruction that sets the model's role/behaviour for the whole request   |
| **User prompt**       | The actual message/request from the human                                |
| **Prompt template**   | A reusable prompt string with `{placeholders}` filled at call time       |
| **Structured output** | Mapping the model's reply straight into a typed Java object              |
| **Streaming**         | Receiving the answer token-by-token instead of all at once               |
| **Ollama**            | A tool that runs open LLMs locally over HTTP (:11434), for free          |

---

## 7. Where to go next

- **README.md** — architecture, running locally, copy-paste `curl`s, interview Q&A.
- **Swagger UI** — <http://localhost:8080/api/swagger-ui.html>