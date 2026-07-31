# 📖 Spring AI, Explained — Semantic Search

A plain-English companion to the code. If the README is "what to run", this is
"why it works". Read it top to bottom; every section builds on the previous one.

---

## 1. The problem semantic search solves

Ordinary search matches **words**. Ask a keyword search "how do I get my money
back?" and it looks for those exact words. If your document says *"refunds are
issued within 14 days"*, keyword search finds **nothing** — no shared words.

Humans don't read that way. We match **meaning**. Semantic search teaches a
computer to do the same, by turning meaning into maths.

```
Keyword search:   "money back"  ==  "money back"      (literal match)
Semantic search:  "money back"  ≈   "refund policy"   (meaning match)
```

---

## 2. Embeddings: turning meaning into numbers

An **embedding model** reads text and outputs a **vector** — a fixed-length list of
numbers. `nomic-embed-text` outputs 768 of them.

```
        ┌─────────────────────┐
"refund"│  embedding model    │──►  [ 0.02, -0.41, 0.85, 0.10, … ]   (768 numbers)
        └─────────────────────┘
```

The key property, learned from vast amounts of text:

> **Texts that mean similar things get vectors that are close together.**

Think of every text as a point in a 768-dimensional space. "refund", "money back"
and "reimbursement" cluster together; "sourdough recipe" sits far away.

You can literally see this with the `/search/embed` endpoint — it prints the vector
length and the first few numbers.

---

## 3. Cosine similarity: measuring "closeness"

Given two vectors, how do we score how close they are? **Cosine similarity** — the
cosine of the angle between them.

```
        ▲                     score = 1.0  (0°)   → identical meaning
        │  b                  score = 0.5  (60°)  → somewhat related
   a ───┼──────►              score = 0.0  (90°)  → unrelated
        │
```

- **1.0** — same direction → same meaning
- **0.0** — perpendicular → unrelated
- It ignores vector *length* and looks only at *direction*, which is why it works
  well regardless of how long or short the texts are.

The `/search/compare` endpoint runs exactly this. Try `cat` vs `kitten` (high) and
`cat` vs `spreadsheet` (low).

---

## 4. The vector store: search by nearest vector

Storing vectors and finding the nearest ones is what a **vector store** does. This
project uses `SimpleVectorStore` — in-memory, brute-force cosine search, zero
setup. The "H2 of vector databases".

```
INDEX (write path)
  text ──► embed ──► store the vector (+ its source label)

SEARCH (read path)
  query ──► embed ──► compare against every stored vector ──► return the closest K
```

Two things worth internalising, both visible in `SemanticSearchEngine`:

1. **`VectorStore.add()` embeds for you.** You hand it text; it calls the embedding
   model and stores the vector. You rarely call the embedder directly.
2. **The same model embeds the query.** `similaritySearch()` embeds your query with
   the *same* model used at index time — otherwise the vectors wouldn't be
   comparable.

---

## 5. How this maps to the code

```
SearchController          → HTTP; knows nothing about Spring AI
      │
      ▼
SemanticSearchEngine      → ★ the ONLY class importing org.springframework.ai.*
      ├─ embed(text)          EmbeddingModel.embed()            (section 2)
      ├─ similarity(a, b)     cosine of two embeddings          (section 3)
      ├─ index(source, texts) VectorStore.add()                 (section 4, write)
      └─ search(q, k, t)      VectorStore.similaritySearch()    (section 4, read)
```

Everything provider-specific is trapped in `SemanticSearchEngine`. The controller
speaks only our `SearchHit`. Swap Ollama → OpenAI (yaml) or SimpleVectorStore →
pgvector (one bean) and nothing else changes. Same isolation move as ZXing in the
QR project and JJWT in the OTP project.

---

## 6. From semantic search to RAG (the whole point)

RAG is **not** a new, separate thing — it is *this* plus two steps you already know
from `01-spring-ai-basics`:

```
SEMANTIC SEARCH (this project)
  question ─► embed ─► similarity search ─► top-K relevant texts
                                                │
RAG (04-ai-rag-service)                         ▼
  … ─► top-K texts ─► paste into a prompt as CONTEXT ─► ChatClient ─► grounded answer
        (retrieve)        (augment)                       (generate)
```

- **Retrieve** — you just built this. `search(...)` is the retrieval step.
- **Augment** — put the retrieved texts into the prompt (a `.user(...)` string).
- **Generate** — call the `ChatClient` (exactly the basics project's `ask`).

So when you open the RAG project, `VectorStoreGateway.search(...)` will feel like
home — it's the same idea you built here. Only "augment + generate" is new, and
that's just a system prompt plus a chat call.

---

## 7. One-paragraph summary

An **embedding** turns text into a vector whose geometry encodes meaning.
**Cosine similarity** scores how aligned two vectors are. A **vector store** keeps
your vectors and, given a query vector, returns the nearest ones. Put together,
that's **semantic search** — matching by meaning, not words — and it is precisely
the retrieval engine that RAG bolts an LLM onto.