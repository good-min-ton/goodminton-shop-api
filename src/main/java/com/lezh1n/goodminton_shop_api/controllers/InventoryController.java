package com.lezh1n.goodminton_shop_api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.common.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.request.SetInventoryRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.InventoryResponse;
import com.lezh1n.goodminton_shop_api.services.InventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PutMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'STORE_ADMIN')")
    public ApiResponse<InventoryResponse> setQuantity(@Valid @RequestBody SetInventoryRequest request) {
        return ApiResponse.<InventoryResponse>builder()
                .result(inventoryService.setQuantity(request))
                .build();
    }

    @GetMapping("/{inventoryId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'STORE_ADMIN')")
    public ApiResponse<InventoryResponse> getById(@PathVariable Integer inventoryId) {
        return ApiResponse.<InventoryResponse>builder()
                .result(inventoryService.getById(inventoryId))
                .build();
    }

    @GetMapping("/stores/{storeId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Page<InventoryResponse>> listByStore(
            @PathVariable Integer storeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ApiResponse.<Page<InventoryResponse>>builder()
                .result(inventoryService.listByStore(storeId, pageable(page, size, sortBy, sortDir)))
                .build();
    }

    @GetMapping("/my-store")
    @PreAuthorize("hasRole('STORE_ADMIN')")
    public ApiResponse<Page<InventoryResponse>> listMyStore(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ApiResponse.<Page<InventoryResponse>>builder()
                .result(inventoryService.listMyStore(pageable(page, size, sortBy, sortDir)))
                .build();
    }

    private Pageable pageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        return PageRequest.of(Math.max(0, page - 1), size, sort);
    }
}
