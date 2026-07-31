package com.dockyard.semanticsearch.ai;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AiBeans — the one place we assemble Spring AI beans. Kept inside the {@code ai}
 * package so that EVERY reference to {@code org.springframework.ai.*} stays in
 * this one corner of the app.
 *
 * <p>{@link EmbeddingModel} — auto-configured by the Spring AI Ollama starter
 * (backed by {@code nomic-embed-text}). It is the thing that turns text into a
 * vector. We do not declare it here; we just receive it.
 *
 * <p>{@link VectorStore} — we use {@link SimpleVectorStore}, an in-memory store
 * that keeps vectors in a map and does a brute-force cosine similarity search.
 * It is the "H2 of vector databases": zero setup, perfect for learning. Swapping
 * it for pgvector / Redis / Qdrant in production is a one-bean change — the
 * {@link SemanticSearchEngine} around it does not change.
 */
@Configuration
public class AiBeans {

    /**
     * The vector store. It needs an {@link EmbeddingModel} because every text
     * added is embedded on the way in, using the SAME model that will later embed
     * the query — vectors are only comparable within one embedding model.
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

}