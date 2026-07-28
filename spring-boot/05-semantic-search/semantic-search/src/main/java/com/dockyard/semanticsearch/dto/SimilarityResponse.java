package com.dockyard.semanticsearch.dto;

/**
 * The cosine similarity between two texts, with a plain-English interpretation so
 * the number is easy to read.
 *
 * @param textA          the first text
 * @param textB          the second text
 * @param score          cosine similarity, 0..1 (1 = same meaning, 0 = unrelated)
 * @param interpretation a human-friendly label for the score
 */
public record SimilarityResponse(
        String textA,
        String textB,
        double score,
        String interpretation
) {}