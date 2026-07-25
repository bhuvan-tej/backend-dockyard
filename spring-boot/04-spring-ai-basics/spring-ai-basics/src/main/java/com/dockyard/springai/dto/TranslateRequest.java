package com.dockyard.springai.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request for the prompt-template demo — a tiny translator.
 *
 * <p>The template lives in the controller ("Translate '{text}' into {language},
 * reply with only the translation."); you just provide the two variables.
 *
 * @param text     what to translate
 * @param language the target language, e.g. "French"
 */
public record TranslateRequest(
        @NotBlank(message = "text is required")
        String text,
        @NotBlank(message = "language is required")
        String language
) {}

