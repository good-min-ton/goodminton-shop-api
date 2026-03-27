package com.lezh1n.goodminton_shop_api.controllers;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.common.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.request.InventoryRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.InventoryByStoreResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.InventoryResponse;
import com.lezh1n.goodminton_shop_api.services.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ApiResponse<InventoryResponse> createInventory(@Valid @RequestBody InventoryRequest request) {
        return ApiResponse.<InventoryResponse>builder()
                .result(inventoryService.createInventory(request))
                .build();
    }

    @PutMapping("/{inventoryId}")
    public ApiResponse<InventoryResponse> updateInventory(@PathVariable Integer inventoryId,
            @Valid @RequestBody InventoryRequest request) {
        return ApiResponse.<InventoryResponse>builder()
                .result(inventoryService.updateInventory(inventoryId, request))
                .build();
    }

    @DeleteMapping("/{inventoryId}")
    public ApiResponse<String> deleteInventory(@PathVariable Integer inventoryId) {
        inventoryService.deleteInventory(inventoryId);
        return ApiResponse.<String>builder()
                .result("Xoá thành công variant khỏi kho")
                .build();
    }

    @GetMapping("/{storeId}")
    public ApiResponse<InventoryByStoreResponse> getInventoriesByStore(
            @PathVariable Integer storeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        return ApiResponse.<InventoryByStoreResponse>builder()
                .result(inventoryService.getInventoriesByStore(storeId, page, size, sortBy, sortDir))
                .build();
    }
}
