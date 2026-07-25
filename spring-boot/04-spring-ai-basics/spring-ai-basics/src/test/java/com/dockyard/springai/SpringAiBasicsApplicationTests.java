package com.dockyard.springai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Context smoke test.
 *
 * <p>The {@link ChatModel} is the only bean that would reach out to Ollama, so we
 * replace it with a mock. If the context still wires up (controller, the ai/
 * engine, Swagger, actuator…), the application is sound — no network needed.
 */
@SpringBootTest
class SpringAiBasicsApplicationTests {

    @MockitoBean
    ChatModel chatModel;   // backs the ChatClient; mocked so no network is hit

    @Test
    void contextLoads() {
        // Passes if the full Spring context starts with the AI bean mocked out.
    }
}