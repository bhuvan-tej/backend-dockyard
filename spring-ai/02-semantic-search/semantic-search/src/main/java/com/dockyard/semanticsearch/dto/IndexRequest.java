package com.dockyard.semanticsearch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request to index one or more texts under a single source label.
 *
 * @param source a label for where these texts came from (echoed back on matches)
 * @param texts  the texts to embed and store (each becomes one searchable vector)
 */
public record IndexRequest(
        @NotBlank(message = "source is required")
        String source,

        @NotEmpty(message = "texts must contain at least one item")
        List<@NotBlank(message = "text items must not be blank") String> texts
) {}