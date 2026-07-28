package com.dockyard.semanticsearch.ai;

import com.dockyard.semanticsearch.exception.AiUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link SemanticSearchEngine} — no Spring context, no network.
 *
 * <p>Two things are worth proving in isolation:
 * <ol>
 *   <li>the cosine-similarity maths is correct (identical vectors → 1.0,
 *       orthogonal vectors → 0.0), and</li>
 *   <li>the isolation boundary holds: any provider failure surfaces as OUR
 *       {@link AiUnavailableException}, never a raw Spring AI exception.</li>
 * </ol>
 */
class SemanticSearchEngineTest {

    private final VectorStore vectorStore = mock(VectorStore.class);

    @Test
    void identicalVectorsHaveSimilarityOne() {
        EmbeddingModel model = mock(EmbeddingModel.class);
        when(model.embed("cat")).thenReturn(new float[]{1f, 0f, 0f});
        when(model.embed("cat again")).thenReturn(new float[]{1f, 0f, 0f});

        SemanticSearchEngine engine = new SemanticSearchEngine(model, vectorStore);

        assertThat(engine.similarity("cat", "cat again")).isCloseTo(1.0, org.assertj.core.data.Offset.offset(1e-6));
    }

    @Test
    void orthogonalVectorsHaveSimilarityZero() {
        EmbeddingModel model = mock(EmbeddingModel.class);
        when(model.embed("up")).thenReturn(new float[]{1f, 0f});
        when(model.embed("side")).thenReturn(new float[]{0f, 1f});

        SemanticSearchEngine engine = new SemanticSearchEngine(model, vectorStore);

        assertThat(engine.similarity("up", "side")).isCloseTo(0.0, org.assertj.core.data.Offset.offset(1e-6));
    }

    @Test
    void wrapsProviderFailureInAiUnavailableException() {
        EmbeddingModel model = mock(EmbeddingModel.class);
        when(model.embed("anything")).thenThrow(new RuntimeException("connection refused"));

        SemanticSearchEngine engine = new SemanticSearchEngine(model, vectorStore);

        assertThatThrownBy(() -> engine.embed("anything"))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("Ollama");
    }
}