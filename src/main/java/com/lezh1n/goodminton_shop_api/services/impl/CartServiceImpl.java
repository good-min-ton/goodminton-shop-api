package com.lezh1n.goodminton_shop_api.services.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.request.CartItemRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.CartItemResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.CartResponse;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.repositories.InventoryRepository;
import com.lezh1n.goodminton_shop_api.services.CartService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final InventoryRepository inventoryRepository;
    private static final String REDIS_KEY = "cart:";

    @Override
    public void addToCart(Integer accountId, CartItemRequest request) {
        String key = REDIS_KEY + accountId;
        CartResponse cart = (CartResponse) redisTemplate.opsForValue().get(key);
        if (cart == null) {
            cart = new CartResponse();
            cart.setItems(new ArrayList<>());
        }

        Integer totalQuantity = inventoryRepository.sumQuantityByVariantSize(request.getVariantSizeId());

        if (totalQuantity == null || totalQuantity < request.getQuantity()) {
            throw new AppException(ErrorCode.ORDER_INVENTORY_INSUFFICIENT);
        }

        Optional<CartItemResponse> existingItem = cart.getItems().stream()
                .filter(item -> item.getVariantSizeId().equals(request.getVariantSizeId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + request.getQuantity());
        } else {
            cart.getItems().add(CartItemResponse.builder()
                    .variantSizeId(request.getVariantSizeId())
                    .quantity(request.getQuantity())
                    .build());
        }
        redisTemplate.opsForValue().set(key, cart, Duration.ofDays(24));
    }

    @Override
    public CartResponse getCart(Integer accountId) {
        String key = REDIS_KEY + accountId;
        return (CartResponse) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void clearCart(Integer accountId) {
        String key = REDIS_KEY + accountId;
        redisTemplate.delete(key);
    }
}
