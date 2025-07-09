package com.lezh1n.goodminton_shop_api.services;

import java.util.List;

import com.lezh1n.goodminton_shop_api.dtos.request.SizeRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.SizeResponse;

public interface SizeService {
    SizeResponse createSize(SizeRequest request);

    SizeResponse getSizeById(Integer sizeId);

    List<SizeResponse> getAllSizes();

    SizeResponse updateSize(Integer sizeId, SizeRequest request);
}
