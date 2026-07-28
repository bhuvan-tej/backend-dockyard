package com.dockyard.semanticsearch.ai;

import com.dockyard.semanticsearch.exception.AiUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SemanticSearchEngine — the ONLY class (besides {@link AiBeans}) that imports
 * {@code org.springframework.ai.*}. It exposes a tiny, domain-shaped API and hides
 * both the {@link EmbeddingModel} and the {@link VectorStore} behind it.
 *
 * <p>Read the methods top-to-bottom; each demonstrates one idea:
 *
 * <pre>
 *   embed(text)                  →  the rawest idea: text becomes a vector of numbers
 *   similarity(a, b)             →  two texts → two vectors → one cosine score
 *   index(source, texts)         →  embed + store many texts (the "write" path)
 *   search(query, topK, thresh)  →  embed the query, return the nearest stored texts
 * </pre>
 *
 * <p>Because everything provider-specific is trapped in this file, the controller
 * never knows whether embeddings come from Ollama, OpenAI or anything else — the
 * same ports-and-adapters move every project in this repo makes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SemanticSearchEngine {

    /** Metadata key we attach to every stored Document so results can be traced. */
    private static final String META_SOURCE = "source";

    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    /** SimpleVectorStore has no size() API, so we count what we add ourselves. */
    private final AtomicInteger indexedCount = new AtomicInteger(0);

    // ------------------------------------------------------------------------
    // 1. embed — the rawest idea in the whole of AI retrieval.
    //    A piece of text goes to the embedding model and comes back as a vector:
    //    a fixed-length array of floats that captures its MEANING. Texts with
    //    similar meaning get numerically similar vectors. This endpoint exists so
    //    you can literally SEE that "text → numbers" is all an embedding is.
    // ------------------------------------------------------------------------
    public float[] embed(String text) {
        try {
            return embeddingModel.embed(text);
        } catch (Exception e) {
            throw providerDown(e);
        }
    }

    // ------------------------------------------------------------------------
    // 2. similarity — embed TWO texts and measure how close their vectors are
    //    using cosine similarity (1.0 = identical direction/meaning, 0 = unrelated).
    //    Try "cat" vs "kitten" (high) against "cat" vs "spreadsheet" (low) to feel
    //    how meaning maps to geometry — the foundation retrieval is built on.
    // ------------------------------------------------------------------------
    public double similarity(String a, String b) {
        try {
            return cosine(embeddingModel.embed(a), embeddingModel.embed(b));
        } catch (Exception e) {
            throw providerDown(e);
        }
    }

    // ------------------------------------------------------------------------
    // 3. index — the "write" path. Each text is embedded and stored, tagged with
    //    its source. VectorStore.add() calls the embedding model FOR us, so adding
    //    a Document both vectorises AND stores it in one step.
    // ------------------------------------------------------------------------
    public int index(String source, List<String> texts) {
        try {
            List<Document> documents = new ArrayList<>(texts.size());
            for (String text : texts) {
                documents.add(Document.builder()
                        .text(text)
                        .metadata(Map.of(META_SOURCE, source))
                        .build());
            }
            vectorStore.add(documents);          // <-- embeds AND stores
            int total = indexedCount.addAndGet(documents.size());
            log.info("Indexed {} text(s) from '{}' (total now {})", documents.size(), source, total);
            return documents.size();
        } catch (Exception e) {
            throw providerDown(e);
        }
    }

    // ------------------------------------------------------------------------
    // 4. search — the "read" path and the payoff. The query is embedded with the
    //    SAME model, then the store ranks stored vectors by cosine similarity and
    //    returns the closest ones above the threshold. This is exactly the "R" in
    //    RAG — the retrieval step, with no LLM attached.
    // ------------------------------------------------------------------------
    public List<SearchHit> search(String query, int topK, double threshold) {
        try {
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(threshold)
                    .build();

            List<Document> hits = vectorStore.similaritySearch(request);
            if (hits == null) {
                return List.of();
            }
            return hits.stream()
                    .map(doc -> new SearchHit(
                            doc.getText(),
                            String.valueOf(doc.getMetadata().getOrDefault(META_SOURCE, "unknown")),
                            doc.getScore() == null ? 0.0 : doc.getScore()))
                    .toList();
        } catch (Exception e) {
            throw providerDown(e);
        }
    }

    /** How many texts are indexed so far. */
    public int count() {
        return indexedCount.get();
    }

    // ------------------------------------------------------------------------
    // Cosine similarity: the dot product of two vectors divided by the product of
    // their magnitudes. It measures the ANGLE between them, ignoring length — so
    // it captures "same direction = same meaning". This is the exact maths the
    // vector store runs internally; we implement it here only to expose it for
    // teaching via the /compare endpoint.
    // ------------------------------------------------------------------------
    private double cosine(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vectors have different dimensions");
        }
        double dot = 0.0, magA = 0.0, magB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            magA += a[i] * a[i];
            magB += b[i] * b[i];
        }
        if (magA == 0 || magB == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }

    /**
     * Turn any Spring AI / provider failure into OUR exception at this one
     * boundary, so the rest of the app never sees a Spring AI type.
     */
    private AiUnavailableException providerDown(Throwable e) {
        log.error("Embedding provider call failed: {}", e.getMessage());
        return new AiUnavailableException(
                "The embedding model is unavailable. Is Ollama running on :11434 " +
                "with 'nomic-embed-text' pulled? Start it with `ollama serve`.", e);
    }

}