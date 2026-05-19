package com.lezh1n.goodminton_shop_api.events;

import java.util.Set;

public record ProductChangedEvent(
        String action,
        Integer productId,
        Set<String> fieldsChanged) {

    private static final Set<String> ALL_SEMANTIC_FIELDS = Set.of(
            "name", "description", "specs", "brand", "category");

    public static ProductChangedEvent created(Integer productId) {
        return new ProductChangedEvent("created", productId, ALL_SEMANTIC_FIELDS);
    }

    public static ProductChangedEvent updated(Integer productId, Set<String> fields) {
        return new ProductChangedEvent("updated", productId, fields);
    }

    public static ProductChangedEvent deleted(Integer productId) {
        return new ProductChangedEvent("deleted", productId, Set.of());
    }
}
