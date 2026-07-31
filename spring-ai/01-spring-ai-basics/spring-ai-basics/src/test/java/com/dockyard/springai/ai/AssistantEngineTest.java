package com.dockyard.springai.ai;

import com.dockyard.springai.exception.AiUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for the isolation boundary: whatever Spring AI throws, callers should
 * only ever see our {@link AiUnavailableException}. We force {@code prompt()} to
 * blow up (simulating "Ollama not running") and assert it is translated.
 *
 * <p>This runs with no network and no Spring context — pure Mockito.
 */
class AssistantEngineTest {

    @Test
    void wrapsProviderFailureInAiUnavailableException() {
        ChatClient chatClient = mock(ChatClient.class);
        when(chatClient.prompt()).thenThrow(new RuntimeException("connection refused"));

        AssistantEngine engine = new AssistantEngine(chatClient);

        assertThatThrownBy(() -> engine.ask("hello"))
                .isInstanceOf(AiUnavailableException.class)
                .hasMessageContaining("Ollama");
    }
}