# 12 · Spring AI

> Based on `04-spring-ai-basics` (ChatClient + structured output),
> `05-semantic-search` (embeddings + vector store), `06-tool-calling` (function
> calling) and `07-ai-rag-service` (embeddings + vector store + RAG).

---

## 🤖 What is Spring AI?

A Spring-idiomatic abstraction over LLM providers (OpenAI, Ollama, etc.). You talk to a
**`ChatClient`** bean instead of raw HTTP, and Spring Boot auto-configures the model from
`application.yml`.

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama3
```

---

## 🧩 The `ChatClient` bean

You register it once in a `@Configuration`:

```java
@Configuration
public class AiBeans {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {   // builder is auto-injected
        return builder
                .defaultSystem("You are a concise, helpful assistant.")
                .build();
    }
}
```

Then use it in a service/engine:

```java
@Component
@RequiredArgsConstructor
public class AssistantEngine {
    private final ChatClient chatClient;

    public String ask(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();               // plain-text answer
    }
}
```

---

## 🧱 The building blocks

| Concept | What it is |
|---------|-----------|
| **Prompt** | the input (system + user messages) sent to the model |
| **System message** | instructions that set behaviour/persona |
| **User message** | the actual question/task |
| **ChatClient** | fluent API to build a prompt and `call()` the model |
| **EmbeddingModel** | turns text → a vector (list of floats) |
| **VectorStore** | stores vectors + text; supports similarity search |

---

## 🎯 Structured output (project 04)

Instead of parsing free text, map the model's answer straight into a Java type:

```java
CountryFacts facts = chatClient.prompt()
        .user("Give facts about " + country)
        .call()
        .entity(CountryFacts.class);      // Spring AI coerces JSON → record

public record CountryFacts(String capital, long population, String currency) {}
```
Spring AI injects format instructions and deserializes the response for you.

---

## 📚 RAG — Retrieval-Augmented Generation (project 05)

**Problem:** an LLM doesn't know your private documents.
**Solution:** retrieve relevant chunks from your data and feed them into the prompt.

```
INGEST (offline)
  document → split into chunks → embed each chunk → store vectors in VectorStore

QUERY (online)
  question → embed → similarity search (top-K chunks) → build prompt with chunks
           → ChatClient.call() → grounded answer + cited sources
```

### Ingestion pipeline

```java
// TextChunker: split long text into overlapping chunks
List<String> chunks = textChunker.chunk(document, chunkSize, overlap);

// IngestionService: embed + store
for (String chunk : chunks) {
    vectorStore.add(new Document(chunk));   // embedding happens under the hood
}
```
- **Chunking** — LLMs/embeddings have token limits; split docs into ~500–1000 char pieces.
- **Overlap** — chunks share a few sentences so context isn't lost at boundaries.

### Retrieval + answer

```java
public ChatResponse ask(String question) {
    // 1. find the most relevant chunks
    List<Document> hits = vectorStore.similaritySearch(
            SearchRequest.query(question).withTopK(props.getTopK()));

    // 2. stuff them into the prompt as context
    String context = hits.stream().map(Document::getText).collect(joining("\n\n"));

    // 3. ask the model, grounded in that context
    String answer = chatClient.prompt()
            .system("Answer ONLY from the context. If unknown, say so.")
            .user(u -> u.text("Context:\n{ctx}\n\nQuestion: {q}")
                        .param("ctx", context).param("q", question))
            .call().content();

    return new ChatResponse(answer, toSources(hits));
}
```

### Why RAG beats fine-tuning for most apps
- **Fresh** — update the vector store, no retraining.
- **Cheaper** — no training runs.
- **Traceable** — you can cite which chunks were used (`SourceChunk`).
- **Private** — your data stays in your store.

---

## ⚙️ Config as properties (`RagProperties`)

Tunables like `chunk-size`, `chunk-overlap`, `top-k` are bound via
`@ConfigurationProperties(prefix = "rag")` — see **[11 – Configuration](./11-configuration-properties.md)**.

---

## 🧯 Resilience

The AI model can be slow or offline. These projects wrap failures in
`AiUnavailableException` and map it to **503 Service Unavailable** in the global handler —
so clients get a clean error instead of a hung request. Add timeouts/retries around
model calls in production.

---

## ✅ Interview soundbites

- "Spring AI gives a provider-agnostic `ChatClient`; swap Ollama ↔ OpenAI via config."
- "Embeddings turn text into vectors; **similarity search** finds semantically close chunks."
- "RAG = **retrieve** relevant context, then **augment** the prompt — grounds answers and reduces hallucination."
- "Structured output maps the LLM response directly into a typed record."

➡️ Next: **[13 – Interview Quick Sheet](./13-interview-quick-sheet.md)**

