package com.lezh1n.goodminton_shop_api.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.dtos.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.request.CreateStoreRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.AccountResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.StoreResponse;
import com.lezh1n.goodminton_shop_api.services.StoreService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ApiResponse<StoreResponse> createStore(CreateStoreRequest request) {
        return ApiResponse.<StoreResponse>builder()
                .result(storeService.createStore(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<StoreResponse> getStoreById(@PathVariable Integer id) {
        return ApiResponse.<StoreResponse>builder()
                .result(storeService.getStoreById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<StoreResponse>> getAllStores() {
        return ApiResponse.<List<StoreResponse>>builder()
                .result(storeService.getAllStores())
                .build();
    }

    @GetMapping("/available-admins")
    public ApiResponse<List<AccountResponse>> getAllAdminsAvailable() {
        return ApiResponse.<List<AccountResponse>>builder()
                .result(storeService.getAllAdminsAvalable())
                .build();
    }

    @PatchMapping("/{storeId}/update-admin/{adminId}")
    public ApiResponse<StoreResponse> updateStoreAdmin(@PathVariable Integer storeId, @PathVariable Integer adminId) {
        return ApiResponse.<StoreResponse>builder()
                .result(storeService.updateStoreAdmin(storeId, adminId))
                .build();
    }
}
