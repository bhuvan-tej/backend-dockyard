package com.dockyard.semanticsearch.dto;

/**
 * One match in a search result.
 *
 * @param text   the matched text
 * @param source where it came from (the label given at index time)
 * @param score  cosine similarity to the query, 0..1 (higher = closer in meaning)
 */
public record SearchResultItem(
        String text,
        String source,
        double score
) {}