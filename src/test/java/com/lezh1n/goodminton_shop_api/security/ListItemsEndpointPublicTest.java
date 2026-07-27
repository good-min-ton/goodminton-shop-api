package com.lezh1n.goodminton_shop_api.security;

import static org.mockito.ArgumentMatchers.anyList;
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

import com.lezh1n.goodminton_shop_api.services.ProductService;

@SpringBootTest
@AutoConfigureMockMvc
class ListItemsEndpointPublicTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ProductService productService;

    @Test
    void listItems_isPublic_returns200WithoutToken() throws Exception {
        when(productService.listItemsByIds(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/api/products/list-items?ids=1,2,3"))
                .andExpect(status().isOk());
    }
}
