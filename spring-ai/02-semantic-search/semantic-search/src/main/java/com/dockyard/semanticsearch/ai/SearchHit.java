package com.dockyard.semanticsearch.ai;

/**
 * SearchHit — a single result from a semantic search, expressed in OUR vocabulary
 * rather than Spring AI's {@code Document}.
 *
 * <p>This is the seam that keeps the library isolated: the controller reasons
 * about {@code SearchHit} values, so it never imports {@code org.springframework.ai.*}.
 * (Same trick as {@code RetrievedChunk} in the RAG project and {@code ParsedToken}
 * in the OTP project.)
 *
 * @param text   the matched text
 * @param source where it came from (the label given at index time)
 * @param score  similarity to the query, 0..1 (higher = more similar in meaning)
 */
public record SearchHit(String text, String source, double score) { }