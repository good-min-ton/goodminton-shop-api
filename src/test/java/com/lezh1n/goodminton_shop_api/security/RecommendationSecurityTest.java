package com.lezh1n.goodminton_shop_api.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.lezh1n.goodminton_shop_api.services.RecommendationService;

@SpringBootTest
@AutoConfigureMockMvc
class RecommendationSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RecommendationService recommendationService;

    @Test
    void recommendations_isPublic_returns200WithoutToken() throws Exception {
        when(recommendationService.getRecommendations(3)).thenReturn(List.of());

        mockMvc.perform(get("/api/products/3/recommendations"))
                .andExpect(status().isOk());
    }
}
