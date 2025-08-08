package com.lezh1n.goodminton_shop_api.dtos.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class OrderAllocationRequest {
    @NotEmpty(message = "ALLOCATION_LIST_EMPTY")
    private List<OrderItemAllocationRequest> allocations;
}
