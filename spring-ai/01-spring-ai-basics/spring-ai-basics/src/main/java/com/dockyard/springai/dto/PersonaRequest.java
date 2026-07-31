package com.dockyard.springai.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request for the system-prompt demo: you supply BOTH the persona (system) and
 * the actual message (user), so you can see how the system prompt steers the
 * reply.
 *
 * @param persona how the assistant should behave, e.g. "You are a terse pirate."
 * @param message the user's actual request
 */
public record PersonaRequest(
        @NotBlank(message = "persona is required")
        String persona,
        @NotBlank(message = "message is required")
        String message
) {}