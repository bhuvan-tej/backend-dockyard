package com.dockyard.semanticsearch;

import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Context smoke test.
 *
 * <p>The {@link EmbeddingModel} is the only bean that would reach out to Ollama,
 * so we replace it with a mock. If the context still wires up (controller, the
 * ai/ engine, the SimpleVectorStore, Swagger, actuator…), the application is
 * sound — no network needed.
 */
@SpringBootTest
class SemanticSearchApplicationTests {

    @MockitoBean
    EmbeddingModel embeddingModel;   // vector store builds on this; mocked so no network

    @Test
    void contextLoads() {
        // Passes if the full Spring context starts with the AI bean mocked out.
    }
}