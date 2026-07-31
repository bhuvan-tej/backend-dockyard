package com.dockyard.semanticsearch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SearchProperties — the tunable search knobs, bound from the {@code search.*}
 * block in application.yml. Centralising them makes retrieval behaviour obvious
 * and changeable without touching code (same idea as RagProperties in the RAG
 * project and OtpProperties in project 03).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "search")
public class SearchProperties {

    /** Default number of nearest matches to return when the caller doesn't specify. */
    private int topK = 5;

    /**
     * Minimum cosine similarity (0..1) a match must reach to be returned. Below
     * this bar a result is considered "not relevant enough" and dropped. Set to 0
     * to always return the topK, however weak the match.
     */
    private double similarityThreshold = 0.0;

}