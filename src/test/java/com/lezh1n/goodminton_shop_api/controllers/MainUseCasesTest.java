package com.lezh1n.goodminton_shop_api.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.lezh1n.goodminton_shop_api.dtos.response.AccountResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.AuthenticationResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.OrderResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductListItemResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.StoreResponse;
import com.lezh1n.goodminton_shop_api.enums.OrderStatus;
import com.lezh1n.goodminton_shop_api.enums.OrderType;
import com.lezh1n.goodminton_shop_api.enums.UserRole;
import com.lezh1n.goodminton_shop_api.services.AuthService;
import com.lezh1n.goodminton_shop_api.services.AccountService;
import com.lezh1n.goodminton_shop_api.services.OrderService;
import com.lezh1n.goodminton_shop_api.services.ProductService;
import com.lezh1n.goodminton_shop_api.services.RecommendationService;
import com.lezh1n.goodminton_shop_api.services.StoreService;

@SpringBootTest
@AutoConfigureMockMvc
class MainUseCasesTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductService productService;

    @MockitoBean
    AuthService authService;

    @MockitoBean
        AccountService accountService;

        @MockitoBean
    OrderService orderService;

        @MockitoBean
        RecommendationService recommendationService;

        @MockitoBean
        StoreService storeService;

    @Test
    void browseProductDetail_isPublicAndReturnsPayload() throws Exception {
        when(productService.getProductById(1)).thenReturn(ProductResponse.builder()
                .id(1)
                .name("Yonex Astrox 99")
                .slug("yonex-astrox-99")
                .build());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1))
                .andExpect(jsonPath("$.result.name").value("Yonex Astrox 99"));
    }

    @Test
    void listItems_isPublicAndReturnsCompactCatalogPayload() throws Exception {
        when(productService.listItemsByIds(List.of(1, 2))).thenReturn(List.of(
                                ProductListItemResponse.builder().id(1).name("Racket A").build(),
                                ProductListItemResponse.builder().id(2).name("Shoes B").build()));

        mockMvc.perform(get("/api/products/list-items?ids=1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].id").value(1))
                .andExpect(jsonPath("$.result[1].name").value("Shoes B"));
    }

    @Test
    void recommendations_arePublicAndReturnSimilarProducts() throws Exception {
        when(recommendationService.getRecommendations(3)).thenReturn(List.of(
                com.lezh1n.goodminton_shop_api.dtos.response.ProductListItemResponse.builder()
                        .id(9)
                        .name("Yonex Astrox 88S")
                        .build(),
                com.lezh1n.goodminton_shop_api.dtos.response.ProductListItemResponse.builder()
                        .id(10)
                        .name("Yonex Astrox 88D")
                        .build()));

        mockMvc.perform(get("/api/products/3/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].id").value(9))
                .andExpect(jsonPath("$.result[1].name").value("Yonex Astrox 88D"));
    }

    @Test
    void storeListing_isPublicAndReturnsStores() throws Exception {
        when(storeService.getAllStores()).thenReturn(List.of(
                StoreResponse.builder().id(1).name("Goodminton Central").isCentral(true).build(),
                StoreResponse.builder().id(2).name("Goodminton District 7").isCentral(false).build()));

        mockMvc.perform(get("/api/stores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].id").value(1))
                .andExpect(jsonPath("$.result[1].name").value("Goodminton District 7"));
    }

    @Test
    void register_isPublicAndCreatesCustomerAccount() throws Exception {
        when(authService.register(any(), any())).thenReturn(AccountResponse.builder()
                .id(7)
                .fullName("Nguyen Van A")
                .email("a@example.com")
                .role(UserRole.CUSTOMER)
                .build());

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Nguyen Van A",
                                  "email": "a@example.com",
                                  "phone": "0912345678",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(7))
                .andExpect(jsonPath("$.result.role").value("CUSTOMER"));
    }

    @Test
    void login_isPublicAndReturnsTokens() throws Exception {
        when(authService.login(any())).thenReturn(AuthenticationResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build());

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": "a@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.accessToken").value("access-token"))
                .andExpect(jsonPath("$.result.refreshToken").value("refresh-token"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createOnlineOrder_requiresCustomerRoleAndReturnsOrder() throws Exception {
        when(orderService.createOnlineOrder(any())).thenReturn(OrderResponse.builder()
                .id(100)
                .status(OrderStatus.PENDING)
                .orderType(OrderType.ONLINE)
                .totalAmount(new BigDecimal("499000"))
                .recipientName("Nguyen Van A")
                .recipientPhone("0912345678")
                .recipientAddress("HCM")
                .build());

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {"variantId": 11, "quantity": 2}
                                  ],
                                  "recipientName": "Nguyen Van A",
                                  "recipientPhone": "0912345678",
                                  "recipientAddress": "HCM",
                                  "recipientEmail": "a@example.com",
                                  "note": "Call before deliver",
                                  "paymentMethod": "PAYOS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(100))
                .andExpect(jsonPath("$.result.status").value("PENDING"))
                .andExpect(jsonPath("$.result.orderType").value("ONLINE"));
    }

    @Test
    void createOnlineOrder_withoutAuth_isRejected() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {"variantId": 11, "quantity": 2}
                                  ],
                                  "recipientName": "Nguyen Van A",
                                  "recipientPhone": "0912345678",
                                  "recipientAddress": "HCM",
                                  "paymentMethod": "PAYOS"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

                @Test
                @WithMockUser(roles = "CUSTOMER")
                void myOrders_isAccessibleForCustomerRole() throws Exception {
                                when(orderService.getMyOrders(any())).thenReturn(org.springframework.data.domain.Page.empty());

                                mockMvc.perform(get("/api/orders/my"))
                                                                .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.result.content").isEmpty());
                }

                @Test
                @WithMockUser(roles = "STORE_ADMIN")
                void createInStoreOrder_isAccessibleForStoreAdmin() throws Exception {
                                when(orderService.createInStoreOrder(any())).thenReturn(OrderResponse.builder()
                                                                .id(200)
                                                                .status(OrderStatus.CONFIRMED)
                                                                .orderType(OrderType.IN_STORE)
                                                                .totalAmount(new BigDecimal("299000"))
                                                                .customerName("Walk-in customer")
                                                                .build());

                                mockMvc.perform(post("/api/orders/in-store")
                                                                                                .with(csrf())
                                                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                                                .content("""
                                                                                                                                {
                                                                                                                                        "customerName": "Walk-in customer",
                                                                                                                                        "customerPhone": "0900000000",
                                                                                                                                        "items": [
                                                                                                                                                {"variantId": 21, "quantity": 1}
                                                                                                                                        ],
                                                                                                                                        "paymentMethod": "COD"
                                                                                                                                }
                                                                                                                                """))
                                                                .andExpect(status().isOk())
                                                                .andExpect(jsonPath("$.result.id").value(200))
                                                                .andExpect(jsonPath("$.result.orderType").value("IN_STORE"))
                                                                .andExpect(jsonPath("$.result.status").value("CONFIRMED"));
                }

                                                    @Test
                                                    @WithMockUser(roles = "STORE_ADMIN")
                                                    void getOrderById_isAccessibleForStoreAdmin() throws Exception {
                                                        when(orderService.getOrderById(55)).thenReturn(OrderResponse.builder()
                                                                .id(55)
                                                                .status(OrderStatus.PENDING)
                                                                .orderType(OrderType.ONLINE)
                                                                .totalAmount(new BigDecimal("499000"))
                                                                .build());

                                                        mockMvc.perform(get("/api/orders/55"))
                                                                .andExpect(status().isOk())
                                                                .andExpect(jsonPath("$.result.id").value(55))
                                                                .andExpect(jsonPath("$.result.status").value("PENDING"));
                                                    }

                                                    @Test
                                                    @WithMockUser(roles = "SUPER_ADMIN")
                                                    void getAllAccounts_isAccessibleForSuperAdmin() throws Exception {
                                                        when(accountService.getAllAccounts(1, 10, "createdAt", "desc", null))
                                                                .thenReturn(Page.empty());

                                                        mockMvc.perform(get("/api/accounts"))
                                                                .andExpect(status().isOk())
                                                                .andExpect(jsonPath("$.result.content").isEmpty());
                                                    }

                                                    @Test
                                                    void getAllAccounts_withoutAuth_isRejected() throws Exception {
                                                        mockMvc.perform(get("/api/accounts"))
                                                                .andExpect(status().isUnauthorized());
                                                    }
}