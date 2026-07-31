package com.dockyard.semanticsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.dockyard.semanticsearch.config.SearchProperties;

/**
 * Semantic Search — the missing step between "chat" and "RAG".
 *
 * <p>The basics project ({@code 04-spring-ai-basics}) taught you to <b>talk</b> to
 * an LLM. RAG ({@code 07-ai-rag-service}) makes an LLM answer from your documents.
 * The hardest new idea in RAG is <b>retrieval</b> — finding the right documents by
 * <i>meaning</i>. This project teaches exactly that idea, on its own, with no LLM
 * to distract you:
 *
 * <ol>
 *   <li><b>embed</b>   — turn a piece of text into a vector (a list of numbers)</li>
 *   <li><b>index</b>   — store those vectors in a vector store</li>
 *   <li><b>search</b>  — given a query, return the most <i>similar</i> stored text</li>
 *   <li><b>compare</b> — see the cosine similarity between any two texts</li>
 * </ol>
 *
 * <p>There is deliberately <b>no chat model</b> here. Semantic search is pure
 * embeddings + vector maths. Once this clicks, RAG is just "do this retrieval,
 * then hand the results to a ChatClient" — which you already know from basics.
 *
 * <p>As in every project in this repo, the third-party engine (Spring AI) is
 * isolated behind our own {@code SemanticSearchEngine} — controllers never
 * import {@code org.springframework.ai.*}.
 */
@SpringBootApplication
@EnableConfigurationProperties(SearchProperties.class)
public class SemanticSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SemanticSearchApplication.class, args);
    }
}