# 🔍 Semantic Search (Spring AI)

## 🎯 Goal

---
Learn the **one idea that makes RAG possible — retrieval by meaning — on its own,
with no LLM to distract you.**

You **index** some texts; the service turns each into an *embedding* (a vector of
numbers that captures its meaning). You **search** with a query; the service
returns the stored texts whose meaning is closest — even when they share no words.

This is the **"R" in R-A-G, extracted**. Once it clicks here, the RAG project is
just "do this retrieval, then hand the results to a `ChatClient`" — which you
already learned in `01-spring-ai-basics`.

> 🧭 **Where this sits in the learning path**
> ```
> 04 spring-ai-basics   ✅ talk to an LLM (one-shot chat)
>         ↓
> 05 semantic-search    ← YOU ARE HERE — retrieval by meaning (no LLM)
>         ↓
> 06 tool-calling       let the LLM call your Java methods
>         ↓
> 07 ai-rag-service     retrieve + augment + generate (now only "A+G" is new)
> ```

## 🤔 What is an embedding? (in plain English)

---
An **embedding** is what you get when a model reads a piece of text and boils its
*meaning* down to a fixed-length list of numbers — a **vector**.

```
"how do I get my money back?"  ──embed──►  [0.021, -0.44, 0.87, … ]  (768 numbers)
"refund policy"                ──embed──►  [0.019, -0.41, 0.85, … ]  ← very close!
"how to bake sourdough"        ──embed──►  [0.77,  0.10, -0.32, … ]  ← far away
```

The magic property: **texts with similar meaning get numerically similar vectors.**
So "find text that means the same thing" becomes "find the nearest vectors" — a
pure geometry problem a database can solve fast. That geometric closeness is
measured with **cosine similarity** (1.0 = same direction/meaning, 0 = unrelated).

> There is deliberately **no chat model** in this project. Semantic search never
> asks an LLM to *write* anything — it only turns text into vectors and compares
> them. That's why it only needs one **embedding** model (`nomic-embed-text`).

## 🏗️ Architecture

---
Same layered, "isolate the third-party engine" design as every other project in the repo:

```
HTTP Request
      │
      ▼
SearchController         thin — validates input, shapes the HTTP response.
      │                  imports NO org.springframework.ai.*
      ▼
SemanticSearchEngine ──► ★ the ONLY class that imports org.springframework.ai.*
      │                    embed · similarity · index · search
      ├─ EmbeddingModel (Spring AI)  ──►  Ollama (nomic-embed-text)  text → vector
      └─ VectorStore    (Spring AI)  ──►  SimpleVectorStore (in-memory nearest-neighbour)
```

```
semantic-search/
├── src/main/java/com/dockyard/semanticsearch/
│   ├── SemanticSearchApplication.java   entry point
│   ├── ai/                              ★ the ONLY package importing Spring AI
│   │   ├── AiBeans.java                 builds the VectorStore bean
│   │   ├── SemanticSearchEngine.java    embed · similarity · index · search
│   │   └── SearchHit.java               our own result type (not Spring AI's)
│   ├── config/
│   │   ├── SearchProperties.java        search.* knobs (top-k, threshold)
│   │   └── OpenApiConfig.java           Swagger UI metadata
│   ├── controller/SearchController.java the 5 endpoints
│   ├── dto/                             request/response records
│   └── exception/                       503 when the model is down, 400 on bad input
├── src/test/java/com/dockyard/semanticsearch/
│   ├── SemanticSearchApplicationTests   context smoke test (EmbeddingModel mocked)
│   └── ai/SemanticSearchEngineTest      cosine maths + error mapping (no network)
├── application.yml                      the ONLY AI config lives here
├── Dockerfile                           multi-stage build → slim non-root JRE
├── docker-compose.yml                   app + Ollama, one command
├── .dockerignore
└── pom.xml
```

**Why isolate Spring AI in `SemanticSearchEngine`?** The controller depends on our
own methods and our `SearchHit`, never on `org.springframework.ai.*`. Swapping
Ollama → OpenAI, or `SimpleVectorStore` → pgvector, is a **pom + yaml/bean change**
— no controller or DTO touched. It also lets us translate any provider failure
into one clean `AiUnavailableException` at a single boundary.

## ✅ Running Locally

> This project needs an **embedding** model provider. The default is **Ollama**,
> which runs models on your own machine for **free — no API key, no cloud bill**.

### Step 1 — install Ollama and pull the embedding model (one time)
```bash
# macOS
brew install ollama
ollama serve                 # starts the local model server on :11434

# in another terminal, pull the embedding model (this project uses ONE model)
ollama pull nomic-embed-text
```

### Step 2 — run the app
```bash
./mvnw spring-boot:run
```
> The app auto-pulls `nomic-embed-text` on first startup if it's missing
> (`pull-model-strategy: when_missing`), so Step 1 is optional if Ollama is running.

Then open **Swagger UI**: <http://localhost:8080/api/swagger-ui.html>

### Option — Docker (app **and** Ollama together, one command)
```bash
docker compose up --build
```
> `nomic-embed-text` is small (~275 MB), so the first-run download is quick
> compared to a chat model. On Apple silicon, native Ollama (above) is faster than
> the container because the container can't use the GPU.

### Prefer OpenAI instead of local models?
```
1. pom.xml    — comment spring-ai-starter-model-ollama, uncomment ...-openai
2. application.yml — replace the `spring.ai.ollama` block with:
     openai:
       api-key: ${OPENAI_API_KEY}
       embedding:
         options:
           model: text-embedding-3-small
3. export OPENAI_API_KEY=sk-...  and run. No Java changes — that's the whole point
   of isolating Spring AI behind SemanticSearchEngine.
```

## 🧪 Trying It Out

### 1. Index a few texts
```bash
curl -X POST http://localhost:8080/api/search/index \
  -H "Content-Type: application/json" \
  -d '{
        "source": "faq",
        "texts": [
          "You can request a refund within 14 days of purchase.",
          "Our office is open Monday to Friday, 9am to 5pm.",
          "Reset your password from the account settings page.",
          "Standard shipping takes 3 to 5 business days."
        ]
      }'
# → {"source":"faq","indexed":4,"totalIndexed":4,"message":"Indexed 4 text(s) from 'faq'"}
```

### 2. Search by MEANING — note the query shares no words with the match
```bash
curl "http://localhost:8080/api/search?q=how%20do%20I%20get%20my%20money%20back"
# → the refund sentence comes first, even though it never says "money back":
# {
#   "query": "how do I get my money back",
#   "count": 4,
#   "results": [
#     {"text":"You can request a refund within 14 days of purchase.","source":"faq","score":0.78},
#     {"text":"Standard shipping takes 3 to 5 business days.","source":"faq","score":0.41},
#     ...
#   ]
# }
```

### 3. See what an embedding actually IS (text → numbers)
```bash
curl "http://localhost:8080/api/search/embed?text=hello%20world"
# → {"text":"hello world","dimensions":768,"preview":[0.021,-0.44,0.87,0.12,...]}
```

### 4. Compare two texts — meaning becomes a single number
```bash
curl "http://localhost:8080/api/search/compare?a=cat&b=kitten"
# → {"textA":"cat","textB":"kitten","score":0.82,"interpretation":"very similar"}

curl "http://localhost:8080/api/search/compare?a=cat&b=spreadsheet"
# → {"textA":"cat","textB":"spreadsheet","score":0.31,"interpretation":"unrelated"}
```

### 5. Index stats
```bash
curl http://localhost:8080/api/search/stats
# → {"source":null,"indexed":0,"totalIndexed":4,"message":"4 text(s) indexed"}
```

### Bonus — the guardrail when the model is down
```bash
# stop `ollama serve`, then:
curl "http://localhost:8080/api/search/embed?text=hi"
# → 503 {"status":503,"message":"The embedding model is unavailable. Is Ollama running..."}
```

## 📋 Endpoints

---
| Method | URL                     | Teaches                          | Success | Errors    |
|--------|-------------------------|----------------------------------|---------|-----------|
| POST   | /api/search/index       | embed + store text               | 200     | 400 / 503 |
| GET    | /api/search             | nearest-neighbour search         | 200     | 503       |
| GET    | /api/search/embed       | what an embedding is (vector)    | 200     | 503       |
| GET    | /api/search/compare     | cosine similarity of two texts   | 200     | 503       |
| GET    | /api/search/stats       | how many texts are indexed       | 200     | —         |
| GET    | /api/actuator/health    | health check                     | 200     | —         |

## 🔑 Key Concepts

---
### Embedding = text → vector of meaning
An embedding model maps text into a point in high-dimensional space (768 numbers
for `nomic-embed-text`). Meaning is encoded as *position*, so similar meaning →
nearby points. The `/embed` endpoint lets you see the raw numbers.

### Cosine similarity
Compares the **angle** between two vectors, ignoring their length. `1.0` = same
direction (same meaning), `0` = perpendicular (unrelated). It's the exact maths the
vector store runs internally; `/compare` exposes it so you can feel it.

### The same model must embed both sides
Different embedding models place text in different vector spaces, so distances are
only meaningful *within one model*. That's why the query and the stored texts are
always embedded by the **same** model — the single most common RAG gotcha.

### Vector store = search by nearest vector
`SimpleVectorStore` keeps vectors in memory and does a brute-force cosine search —
"the H2 of vector databases". `VectorStore.add()` embeds text for you on the way
in; `similaritySearch()` embeds the query and ranks by closeness. Swap it for
pgvector / Redis / Qdrant in production without touching the engine's callers.

### topK and threshold
`topK` caps how many matches come back; `threshold` (0..1) drops matches that
aren't similar enough. In RAG, that threshold becomes the "I don't know" guardrail.

## 🧰 Tech Stack

---
| Tool                  | Purpose                                        |
|-----------------------|------------------------------------------------|
| Java 21 (LTS)         | Language                                       |
| Spring Boot 3.5       | Web, Validation, Actuator                      |
| Spring AI 1.0         | `EmbeddingModel` + `VectorStore` (isolated)    |
| Ollama (default)      | Local, free embedding model — no API key       |
| SimpleVectorStore     | Zero-setup in-memory vector store              |
| SpringDoc OpenAPI     | Swagger UI                                     |
| Lombok                | Boilerplate reduction                          |
| JUnit 5 + Mockito     | Tests (model mocked — no network)              |
| Docker (multi-stage)  | Slim, non-root runtime image + bundled Ollama  |

## 💡 Interview Questions

---
**Q: What is an embedding?**
> A vector (fixed-length list of numbers) that a model produces from text such that
> the geometry encodes meaning — similar text lands near similar text. It turns
> "find text with the same meaning" into "find the nearest vectors".

**Q: How is semantic search different from keyword search?**
> Keyword search matches literal tokens; semantic search matches *meaning*. "how do
> I get my money back" can retrieve a "refund policy" sentence that shares no words,
> because their embeddings are close.

**Q: Why must the same model embed both the documents and the query?**
> Each embedding model defines its own vector space. Vectors from different models
> aren't comparable, so distances become meaningless. Embedding both sides with one
> model keeps the geometry consistent.

**Q: What is cosine similarity and why use it over Euclidean distance?**
> It measures the angle between two vectors, ignoring magnitude, so it focuses on
> *direction* (meaning) rather than length. It's stable across texts of different
> lengths, which is why it's the default for embedding comparison.

**Q: How does this relate to RAG?**
> This IS the retrieval step of RAG. RAG adds two things on top: *augment* (paste
> the retrieved texts into a prompt as context) and *generate* (an LLM answers from
> that context). Master retrieval here and RAG is a small addition.

**Q: `SimpleVectorStore` is in-memory — how would you productionise it?**
> Swap the one `VectorStore` bean for a persistent store (pgvector, Redis, Qdrant —
> Spring AI has starters for each); nothing else changes. Add batched ingestion,
> metadata filtering, persistence, and a re-ranking step after retrieval.

## 🔗 Where to go next

- **[SPRING_AI_EXPLAINED.md](SPRING_AI_EXPLAINED.md)** — the same ideas with diagrams,
  and exactly how this grows into RAG.