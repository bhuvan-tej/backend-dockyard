package com.dockyard.springai.ai;

import com.dockyard.springai.exception.AiUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * The ONLY class (besides {@link AiBeans}) that imports {@code org.springframework.ai.*}.
 *
 * <p>Every way you can talk to an LLM through Spring AI is demonstrated here as
 * one small method. Read them top-to-bottom — each adds exactly one new idea to
 * the previous:
 *
 * <pre>
 *   ask        →  the bare minimum: text in, text out
 *   askAs      →  + a system prompt to set the model's behaviour/persona
 *   askTemplate→  + a reusable prompt with {placeholders} filled at call time
 *   askFor     →  + map the reply straight into a typed Java object
 *   stream     →  + receive the answer token-by-token instead of all at once
 * </pre>
 *
 * <p>Because everything provider-specific is trapped in this file, the rest of
 * the app (controllers, DTOs) never knows whether the model is Ollama, OpenAI or
 * anything else — the same ports-and-adapters move the QR, OTP and RAG projects
 * make with their third-party engines.
 */
@Slf4j
@Component
public class AssistantEngine {

    private final ChatClient chat;

    public AssistantEngine(ChatClient chat) {
        this.chat = chat;
    }

    // ------------------------------------------------------------------------
    // 1. ask — the "hello world" of Spring AI: a message goes in, text comes out
    // ------------------------------------------------------------------------
    public String ask(String message) {
        try {
            return chat.prompt()      // start building a request
                    .user(message)    // the human's message
                    .call()           // send it and block for the reply
                    .content();       // pull out the plain-text answer
        } catch (Exception e) {
            throw providerDown(e);
        }
    }

    // ------------------------------------------------------------------------
    // 2. askAs — a SYSTEM prompt sets the rules/persona for every user message.
    //    The model reads it as "who you are / how to behave", the user message
    //    as "the actual request". This is how you make a "pirate", a "concise
    //    senior engineer", a "JSON-only bot", etc.
    // ------------------------------------------------------------------------
    public String askAs(String systemPrompt, String userMessage) {
        try {
            return chat.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception e) {
            throw providerDown(e);
        }
    }

    // ------------------------------------------------------------------------
    // 3. askTemplate — a reusable prompt with {placeholders}. Spring AI fills the
    //    params in for you, so you keep a stable, tested prompt and only vary the
    //    inputs. Example template: "Translate '{text}' into {language}."
    // ------------------------------------------------------------------------
    public String askTemplate(String template, Map<String, Object> variables) {
        try {
            return chat.prompt()
                    .user(spec -> spec.text(template).params(variables))
                    .call()
                    .content();
        } catch (Exception e) {
            throw providerDown(e);
        }
    }

    // ------------------------------------------------------------------------
    // 4. askFor — STRUCTURED OUTPUT. Instead of a String you ask for a Java type;
    //    Spring AI injects "reply as JSON matching this schema", then parses the
    //    reply into your object. No manual JSON handling. Great for extraction.
    //
    //    Small local models (e.g. qwen2.5) are unreliable at free-form JSON:
    //    they rename fields, wrap the reply in prose/markdown, or — as we hit here
    //    — simply stop before the closing brace, so parsing fails with
    //    "Unexpected end-of-input". Because temperature 0 is deterministic, the
    //    same broken reply comes back every time.
    //
    //    The fix is to stop relying on free-form text and use the provider's
    //    NATIVE JSON mode. `format("json")` grammar-constrains Ollama so the reply
    //    is ALWAYS syntactically complete/valid JSON; `.entity()` still supplies
    //    the schema instruction so the fields match our record. temperature 0 =
    //    deterministic, numPredict 500 = ample room to finish.
    //
    //    NOTE: this is the one spot that uses an Ollama-specific option (JSON mode
    //    has no portable equivalent yet). For OpenAI you'd use its response-format
    //    ("json_object") instead — a small, localised change confined to here.
    // ------------------------------------------------------------------------
    public <T> T askFor(String message, Class<T> type) {
        try {
            return chat.prompt()
                    .system("""
                            You are a precise data API, not a chatbot.
                            Reply with ONLY a single JSON object that fills every
                            requested field with real, factual values.
                            No prose, no explanation, no markdown code fences.""")
                    .user(message)
                    .options(OllamaOptions.builder()
                            .temperature(0.0)
                            .numPredict(500)
                            .format("json")   // ← native JSON mode: guaranteed valid JSON
                            .build())
                    .call()
                    .entity(type);   // ← the magic: reply mapped to your class
        } catch (Exception e) {
            throw providerDown(e);
        }
    }

    // ------------------------------------------------------------------------
    // 5. stream — the answer arrives token-by-token as a reactive stream. This is
    //    what powers the "typing" effect in chat UIs. Returns a Flux the
    //    controller can hand to the browser as Server-Sent Events.
    // ------------------------------------------------------------------------
    public Flux<String> stream(String message) {
        return chat.prompt()
                .user(message)
                .stream()            // ← stream instead of call
                .content()
                .onErrorMap(this::providerDown);
    }

    /**
     * Turn any Spring AI / provider failure into OUR exception at this one
     * boundary, so the rest of the app never sees a Spring AI type.
     */
    private AiUnavailableException providerDown(Throwable e) {
        log.error("AI provider call failed: {}", e.getMessage());
        return new AiUnavailableException(
                "The AI model is unavailable. Is Ollama running on :11434? " +
                "Start it with `ollama serve`.", e);
    }
}