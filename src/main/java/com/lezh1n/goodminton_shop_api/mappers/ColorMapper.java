package com.lezh1n.goodminton_shop_api.mappers;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.ColorRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ColorResponse;
import com.lezh1n.goodminton_shop_api.entities.Color;

@Component
public class ColorMapper {
    public Color toColor(ColorRequest request) {
        return Color.builder()
                .name(request.getName())
                .build();
    }

    public ColorResponse toColorResponse(Color color) {
        return ColorResponse.builder()
                .colorId(color.getColorId())
                .name(color.getName())
                .build();
    }
}
