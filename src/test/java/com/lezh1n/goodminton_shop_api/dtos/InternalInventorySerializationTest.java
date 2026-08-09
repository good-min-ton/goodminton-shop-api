package com.lezh1n.goodminton_shop_api.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lezh1n.goodminton_shop_api.dtos.request.UpdateStoreRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.InventoryByStoreResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.StoreResponse;

/**
 * The central-store flag is a contract in two directions: the RAG chatbot reads
 * it to decide whether an order can be placed at all, and the admin UI round-trips
 * it when editing a store. Jackson derives the wire name differently for a record
 * component, a primitive Lombok getter and a wrapper Lombok getter, so these
 * assertions pin the name that all three must agree on.
 */
class InternalInventorySerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("inventory row exposes the central flag as \"isCentral\"")
    void inventoryRowSerialisesCentralFlag() throws Exception {
        String json = mapper.writeValueAsString(
                new InventoryByStoreResponse(1, "Goodminton HQ - Di An", true, 12));

        assertThat(json)
                .contains("\"isCentral\":true")
                .contains("\"storeId\":1")
                .contains("\"storeName\":\"Goodminton HQ - Di An\"")
                .contains("\"quantity\":12");
    }

    @Test
    @DisplayName("a branch row is not flagged central")
    void branchRowIsNotCentral() throws Exception {
        String json = mapper.writeValueAsString(
                new InventoryByStoreResponse(2, "Chi nhanh Q7", false, 3));

        assertThat(json).contains("\"isCentral\":false");
    }

    @Test
    @DisplayName("StoreResponse sends isCentral, matching what the update request accepts")
    void storeResponseAndRequestAgreeOnTheName() throws Exception {
        String json = mapper.writeValueAsString(
                StoreResponse.builder().id(1).name("HQ").isCentral(true).build());

        // Without @JsonProperty this serialises as "central": the field is a
        // primitive, so Lombok generates isCentral() and Jackson strips the "is".
        // The request DTO uses a Boolean wrapper, whose getIsCentral() keeps it -
        // so the API would accept isCentral and answer with central, and a client
        // reading back what it sent would get undefined.
        assertThat(json).contains("\"isCentral\":true").doesNotContain("\"central\"");

        UpdateStoreRequest parsed =
                mapper.readValue("{\"isCentral\":true}", UpdateStoreRequest.class);
        assertThat(parsed.getIsCentral()).isTrue();
    }
}
