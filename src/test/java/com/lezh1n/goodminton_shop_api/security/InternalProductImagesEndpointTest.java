package com.lezh1n.goodminton_shop_api.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.lezh1n.goodminton_shop_api.dtos.response.ResourceResponse;
import com.lezh1n.goodminton_shop_api.enums.ResourceOwner;
import com.lezh1n.goodminton_shop_api.services.ResourceService;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.internal-api-key=test-internal-key")
class InternalProductImagesEndpointTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ResourceService resourceService;

    @Test
    void images_withValidKey_returns200AndImages() throws Exception {
        when(resourceService.listByOwner(ResourceOwner.PRODUCT_THUMBNAIL, 5))
                .thenReturn(List.of(
                        ResourceResponse.builder().id(9).url("http://img/9").sortOrder(0).build(),
                        ResourceResponse.builder().id(10).url("http://img/10").sortOrder(1).build()));

        mockMvc.perform(get("/api/internal/products/5/images")
                        .header("X-Internal-Key", "test-internal-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resourceId").value(9))
                .andExpect(jsonPath("$[0].url").value("http://img/9"))
                .andExpect(jsonPath("$[0].sortOrder").value(0))
                .andExpect(jsonPath("$[1].resourceId").value(10));
    }

    @Test
    void images_withoutKey_returns401() throws Exception {
        mockMvc.perform(get("/api/internal/products/5/images"))
                .andExpect(status().isUnauthorized());
    }
}
