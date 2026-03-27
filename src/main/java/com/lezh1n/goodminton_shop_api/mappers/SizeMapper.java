package com.lezh1n.goodminton_shop_api.mappers;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.SizeRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.SizeResponse;
import com.lezh1n.goodminton_shop_api.entities.Size;

@Component
public class SizeMapper {
    public Size toSize(SizeRequest request) {
        return Size.builder()
                .name(request.getName())
                .type(request.getType())
                .build();
    }

    public SizeResponse toSizeResponse(Size size) {
        return SizeResponse.builder()
                .sizeId(size.getId())
                .name(size.getName())
                .type(size.getType())
                .build();
    }
}
