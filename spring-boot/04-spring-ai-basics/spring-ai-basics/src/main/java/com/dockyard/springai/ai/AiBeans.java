package com.dockyard.springai.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The one place we assemble Spring AI beans.
 *
 * <p>Spring AI's Ollama starter auto-configures a {@link ChatClient.Builder} for
 * us (pointed at whatever provider is set in {@code application.yml}). We turn
 * that builder into a ready-to-use {@link ChatClient} — the single object our
 * {@link AssistantEngine} calls.
 *
 * <p>Swapping Ollama for OpenAI (or any provider) changes the pom + yaml only;
 * this class and {@link AssistantEngine} stay exactly the same, because they
 * depend on the {@code ChatClient} <i>interface</i>, not on any provider.
 */
@Configuration
public class AiBeans {

    /**
     * A single, application-wide {@link ChatClient}.
     *
     * <p>{@code ChatClient} is Spring AI's high-level, fluent entry point — think
     * of it as the {@code RestClient}/{@code JdbcTemplate} of the AI world. You
     * could also inject the lower-level {@code ChatModel} directly, but the
     * builder gives us the nice {@code .prompt().user(...).call()} DSL.
     *
     * @param builder auto-configured by the Spring AI Ollama starter
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}