package com.dockyard.semanticsearch.dto;

import java.util.List;

/**
 * Shows what an embedding actually IS: a text turned into a long list of numbers.
 * A full vector (e.g. 768 numbers) is unwieldy, so we return its length plus a
 * short preview of the first few values.
 *
 * @param text       the text that was embedded
 * @param dimensions the vector's length (how many numbers describe this text)
 * @param preview    the first few numbers of the vector, as a taste
 */
public record EmbeddingResponse(
        String text,
        int dimensions,
        List<Float> preview
) {}