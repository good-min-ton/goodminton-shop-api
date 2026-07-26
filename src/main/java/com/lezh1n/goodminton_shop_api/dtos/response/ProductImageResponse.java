package com.lezh1n.goodminton_shop_api.dtos.response;

// Internal image-list item for RAG's ImageIndexer. Field names are a cross-repo contract.
public record ProductImageResponse(Integer resourceId, String url, Integer sortOrder) {
}
