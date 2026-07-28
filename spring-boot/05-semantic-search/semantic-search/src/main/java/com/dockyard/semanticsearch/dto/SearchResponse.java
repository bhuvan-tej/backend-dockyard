package com.dockyard.semanticsearch.dto;

import java.util.List;

/**
 * The response to a semantic search.
 *
 * @param query   the query that was searched for
 * @param count   how many matches were returned
 * @param results the matches, most similar first
 */
public record SearchResponse(
        String query,
        int count,
        List<SearchResultItem> results
) {}