package com.lezh1n.goodminton_shop_api.services;

import java.util.List;

import com.lezh1n.goodminton_shop_api.dtos.request.ColorRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ColorResponse;

public interface ColorService {
    ColorResponse createColor(ColorRequest request);

    ColorResponse getColorById(Integer colorId);

    List<ColorResponse> getAllColors();

    ColorResponse updateColor(Integer colorId, ColorRequest request);

    void deleteColor(Integer colorId);
}
