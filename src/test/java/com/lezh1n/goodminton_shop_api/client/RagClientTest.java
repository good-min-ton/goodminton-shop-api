package com.lezh1n.goodminton_shop_api.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.SocketTimeoutException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RagClientTest {

    private MockRestServiceServer server;
    private RagClient ragClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://rag-test");
        server = MockRestServiceServer.bindTo(builder).build();
        ragClient = new RagClient(builder.build());
    }

    @Test
    void similar_parsesIdsAndSimilarityInOrder() {
        String json = """
                {
                  "product_id": "3",
                  "count": 2,
                  "results": [
                    {"product_id": "9",  "name": "A", "similarity": 0.977, "distance": 0.023, "chunk_count": 3},
                    {"product_id": "12", "name": "B", "similarity": 0.812, "distance": 0.188, "chunk_count": 2}
                  ]
                }
                """;
        server.expect(requestTo("http://rag-test/products/3/similar?limit=20"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<RagCandidate> result = ragClient.similar(3, 20);

        server.verify();
        assertThat(result).extracting(RagCandidate::productId).containsExactly(9, 12);
        assertThat(result.get(0).similarity()).isEqualTo(0.977);
    }

    @Test
    void similar_skipsUnparseableProductId() {
        String json = """
                {
                  "product_id": "3",
                  "count": 2,
                  "results": [
                    {"product_id": "abc", "name": "A", "similarity": 0.9, "distance": 0.1, "chunk_count": 1},
                    {"product_id": "9",   "name": "B", "similarity": 0.8, "distance": 0.2, "chunk_count": 1}
                  ]
                }
                """;
        server.expect(requestTo("http://rag-test/products/3/similar?limit=20"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<RagCandidate> result = ragClient.similar(3, 20);

        server.verify();
        // Bad entry ("abc") is skipped with a WARN, the valid one survives, no throw.
        assertThat(result).extracting(RagCandidate::productId).containsExactly(9);
    }

    @Test
    void similar_returnsEmptyOn404() {
        server.expect(requestTo("http://rag-test/products/3/similar?limit=20"))
              .andRespond(withStatus(HttpStatus.NOT_FOUND)
                      .body("{\"detail\":\"Không tìm thấy sản phẩm\"}")
                      .contentType(MediaType.APPLICATION_JSON));

        assertThat(ragClient.similar(3, 20)).isEmpty();
    }

    @Test
    void similar_returnsEmptyOnTimeout() {
        server.expect(requestTo("http://rag-test/products/3/similar?limit=20"))
              .andRespond(withException(new SocketTimeoutException("read timed out")));

        assertThat(ragClient.similar(3, 20)).isEmpty();
    }

    @Test
    void similar_returnsEmptyOnMalformedJson() {
        server.expect(requestTo("http://rag-test/products/3/similar?limit=20"))
              .andRespond(withSuccess("{ not json", MediaType.APPLICATION_JSON));

        assertThat(ragClient.similar(3, 20)).isEmpty();
    }
}
