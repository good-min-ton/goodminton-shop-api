package com.lezh1n.goodminton_shop_api.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.CreateStoreRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.StoreResponse;
import com.lezh1n.goodminton_shop_api.entities.Store;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StoreMapper {

    private final AccountMapper accountMapper;

    public Store toStore(CreateStoreRequest request) {
        return Store.builder()
                .name(request.getName())
                .address(request.getAddress())
                .contact(request.getContact())
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .createAt(LocalDateTime.now())
                .build();
    }

    public StoreResponse toStoreResponse(Store store) {
        return StoreResponse.builder()
                .storeId(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .contact(store.getContact())
                .longitude(store.getLongitude())
                .latitude(store.getLatitude())
                .createAt(store.getCreateAt())
                .admin(accountMapper.toAccountResponse(store.getAdmin()))
                .build();
    }
}
