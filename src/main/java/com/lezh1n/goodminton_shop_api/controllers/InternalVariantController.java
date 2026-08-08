package com.lezh1n.goodminton_shop_api.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.dtos.response.InventoryByStoreResponse;
import com.lezh1n.goodminton_shop_api.repositories.InventoryRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/internal/variants")
@RequiredArgsConstructor
public class InternalVariantController {

    private final InventoryRepository inventoryRepository;

    // The central flag rides along on each row: the RAG chatbot needs to know
    // which quantity is actually orderable, and reading it from a separate call
    // would let it pair fresh stock with a stale central-store id.
    @GetMapping("/{variantId}/inventory")
    public List<InventoryByStoreResponse> getInventory(@PathVariable Integer variantId) {
        return inventoryRepository.findByVariant_Id(variantId).stream()
                .map(i -> new InventoryByStoreResponse(
                        i.getStore().getId(),
                        i.getStore().getName(),
                        i.getStore().isCentral(),
                        i.getQuantity()))
                .toList();
    }
}
