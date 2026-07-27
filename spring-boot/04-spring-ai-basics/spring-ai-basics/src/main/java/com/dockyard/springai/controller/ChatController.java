package com.dockyard.springai.controller;

import com.dockyard.springai.ai.AssistantEngine;
import com.dockyard.springai.dto.AskRequest;
import com.dockyard.springai.dto.AskResponse;
import com.dockyard.springai.dto.CountryFacts;
import com.dockyard.springai.dto.PersonaRequest;
import com.dockyard.springai.dto.TranslateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * A thin HTTP layer over {@link AssistantEngine}. Each endpoint maps 1:1 to one
 * Spring AI idea, so you can try them in order and watch the concepts build up.
 *
 * <p>Notice this class imports NO {@code org.springframework.ai.*} — it only
 * knows our {@code AssistantEngine} and our DTOs. That's the isolation boundary.
 */
@Tag(name = "Chat", description = "The five core Spring AI patterns, one endpoint each")
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final AssistantEngine assistant;

    public ChatController(AssistantEngine assistant) {
        this.assistant = assistant;
    }

    // 1. Simplest possible call: message in, answer out.
    @Operation(summary = "Ask", description = "Send a message, get a plain-text answer.")
    @PostMapping("/ask")
    public AskResponse ask(@Valid @RequestBody AskRequest request) {
        return new AskResponse(assistant.ask(request.message()));
    }

    // 2. System prompt: the persona steers HOW the model answers.
    @Operation(summary = "Persona (system prompt)",
            description = "Set a system prompt (persona) that governs the reply.")
    @PostMapping("/persona")
    public AskResponse persona(@Valid @RequestBody PersonaRequest request) {
        return new AskResponse(assistant.askAs(request.persona(), request.message()));
    }

    // 3. Prompt template: a fixed prompt with {placeholders} filled per request.
    @Operation(summary = "Template (translator)",
            description = "A reusable prompt template with {text} and {language} filled in.")
    @PostMapping("/translate")
    public AskResponse translate(@Valid @RequestBody TranslateRequest request) {
        String template = "Translate '{text}' into {language}. "
                + "Reply with ONLY the translation, no quotes, no explanation.";
        String answer = assistant.askTemplate(template, Map.of(
                "text", request.text(),
                "language", request.language()));
        return new AskResponse(answer);
    }

    // 4. Structured output: get a typed object back instead of a String.
    @Operation(summary = "Structured output",
            description = "Ask about a country; Spring AI parses the reply into a CountryFacts object.")
    @GetMapping("/country")
    public CountryFacts country(@RequestParam(defaultValue = "Japan") String name) {
        return assistant.askFor(
                "Give me quick facts about the country: " + name, CountryFacts.class);
    }

    // 5. Streaming: the answer arrives token-by-token as Server-Sent Events.
    @Operation(summary = "Stream",
            description = "Stream the answer token-by-token (Server-Sent Events).")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String message) {
        return assistant.stream(message);
    }

}