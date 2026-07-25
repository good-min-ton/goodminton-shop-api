package com.lezh1n.goodminton_shop_api.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RagClient {

    private final RestClient ragRestClient;

    public RagClient(RestClient ragRestClient) {
        this.ragRestClient = ragRestClient;
    }

    /**
     * Calls RAG GET /products/{productId}/similar?limit={limit}.
     * Returns candidates ordered as the RAG service ranked them.
     * NEVER throws: any error (timeout, non-2xx incl. 404, connection refused,
     * parse error) yields an empty list and a WARN log. A single result whose
     * product_id is not an integer is skipped (WARN) without discarding the rest.
     */
    public List<RagCandidate> similar(int productId, int limit) {
        try {
            SimilarProductsResponse body = ragRestClient.get()
                    .uri("/products/{productId}/similar?limit={limit}", productId, limit)
                    .retrieve()
                    .body(SimilarProductsResponse.class);
            if (body == null || body.results() == null) {
                return List.of();
            }
            List<RagCandidate> out = new ArrayList<>(body.results().size());
            for (SimilarResult r : body.results()) {
                try {
                    out.add(new RagCandidate(Integer.parseInt(r.productId()), r.similarity()));
                } catch (NumberFormatException ex) {
                    log.warn("RAG returned unparseable product_id '{}', skipping", r.productId());
                }
            }
            return out;
        } catch (Exception ex) {
            log.warn("RAG similar lookup failed for productId={}: {}", productId, ex.toString());
            return List.of();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SimilarProductsResponse(
            @JsonProperty("product_id") String productId,
            @JsonProperty("count") int count,
            @JsonProperty("results") List<SimilarResult> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SimilarResult(
            @JsonProperty("product_id") String productId,
            @JsonProperty("name") String name,
            @JsonProperty("similarity") double similarity,
            @JsonProperty("distance") double distance,
            @JsonProperty("chunk_count") int chunkCount) {
    }
}
