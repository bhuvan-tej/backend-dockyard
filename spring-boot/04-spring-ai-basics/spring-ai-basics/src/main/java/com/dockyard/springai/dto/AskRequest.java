package com.dockyard.springai.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * A single message from the user, used by /ask and /stream.
 *
 * @param message the human's question or instruction
 */
public record AskRequest(
        @NotBlank(message = "message is required")
        String message
) {}