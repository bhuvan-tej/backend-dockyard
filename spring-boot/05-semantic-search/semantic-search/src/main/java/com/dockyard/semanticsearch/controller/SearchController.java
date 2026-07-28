package com.dockyard.semanticsearch.controller;

import com.dockyard.semanticsearch.ai.SearchHit;
import com.dockyard.semanticsearch.ai.SemanticSearchEngine;
import com.dockyard.semanticsearch.config.SearchProperties;
import com.dockyard.semanticsearch.dto.EmbeddingResponse;
import com.dockyard.semanticsearch.dto.IndexRequest;
import com.dockyard.semanticsearch.dto.IndexResponse;
import com.dockyard.semanticsearch.dto.SearchResponse;
import com.dockyard.semanticsearch.dto.SearchResultItem;
import com.dockyard.semanticsearch.dto.SimilarityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * A thin HTTP layer over {@link SemanticSearchEngine}. Each endpoint maps to one
 * idea, so you can try them in order and watch semantic search build up from
 * "text → vector" all the way to "find the nearest meaning".
 *
 * <p>Notice this class imports NO {@code org.springframework.ai.*} — it only knows
 * our {@code SemanticSearchEngine}, our {@code SearchHit} and our DTOs. That's the
 * isolation boundary.
 */
@Tag(name = "Semantic Search", description = "Search text by meaning using embeddings — no LLM")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    /** How many numbers of the raw vector to show in the /embed preview. */
    private static final int PREVIEW_SIZE = 8;

    private final SemanticSearchEngine engine;
    private final SearchProperties properties;

    // 1. Index: embed and store some texts so they become searchable.
    @Operation(summary = "Index texts",
            description = "Embed one or more texts and store them under a source label.")
    @PostMapping("/index")
    public IndexResponse index(@Valid @RequestBody IndexRequest request) {
        int added = engine.index(request.source(), request.texts());
        return new IndexResponse(
                request.source(), added, engine.count(),
                "Indexed %d text(s) from '%s'".formatted(added, request.source()));
    }

    // 2. Search: the payoff. Find the stored texts closest in MEANING to a query.
    @Operation(summary = "Search by meaning",
            description = "Return the stored texts most similar to the query (the 'R' of RAG).")
    @GetMapping
    public SearchResponse search(
            @RequestParam String q,
            @RequestParam(required = false) Integer topK,
            @RequestParam(required = false) Double threshold) {

        int k = topK != null ? topK : properties.getTopK();
        double t = threshold != null ? threshold : properties.getSimilarityThreshold();

        List<SearchResultItem> results = engine.search(q, k, t).stream()
                .map(this::toItem)
                .toList();

        return new SearchResponse(q, results.size(), results);
    }

    // 3. Embed: see what an embedding actually is — a text turned into numbers.
    @Operation(summary = "Show an embedding",
            description = "Return the vector length and a preview of the numbers a text becomes.")
    @GetMapping("/embed")
    public EmbeddingResponse embed(@RequestParam String text) {
        float[] vector = engine.embed(text);
        List<Float> preview = new java.util.ArrayList<>();
        for (int i = 0; i < Math.min(PREVIEW_SIZE, vector.length); i++) {
            preview.add(vector[i]);
        }
        return new EmbeddingResponse(text, vector.length, preview);
    }

    // 4. Compare: cosine similarity between two texts — meaning becomes a number.
    @Operation(summary = "Compare two texts",
            description = "Cosine similarity (0..1) between two texts: 1 = same meaning, 0 = unrelated.")
    @GetMapping("/compare")
    public SimilarityResponse compare(@RequestParam String a, @RequestParam String b) {
        double score = engine.similarity(a, b);
        return new SimilarityResponse(a, b, round(score), interpret(score));
    }

    // 5. Stats: how many texts are currently indexed.
    @Operation(summary = "Index stats", description = "How many texts are currently indexed.")
    @GetMapping("/stats")
    public IndexResponse stats() {
        int total = engine.count();
        return new IndexResponse(null, 0, total, "%d text(s) indexed".formatted(total));
    }

    private SearchResultItem toItem(SearchHit hit) {
        return new SearchResultItem(hit.text(), hit.source(), round(hit.score()));
    }

    private double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }

    private String interpret(double score) {
        if (score >= 0.8) return "very similar";
        if (score >= 0.6) return "related";
        if (score >= 0.4) return "loosely related";
        return "unrelated";
    }

}