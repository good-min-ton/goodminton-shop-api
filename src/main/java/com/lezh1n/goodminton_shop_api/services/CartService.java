package com.lezh1n.goodminton_shop_api.services;

import com.lezh1n.goodminton_shop_api.dtos.request.CartItemRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.CartResponse;

public interface CartService {
    void addToCart(Integer accountId, CartItemRequest request);
    CartResponse getCart(Integer accountId);
    void clearCart(Integer accountId);
}
