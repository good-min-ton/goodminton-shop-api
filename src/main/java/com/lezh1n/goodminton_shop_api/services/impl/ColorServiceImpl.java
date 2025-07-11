package com.lezh1n.goodminton_shop_api.services.impl;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.request.ColorRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ColorResponse;
import com.lezh1n.goodminton_shop_api.entities.Color;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.ColorMapper;
import com.lezh1n.goodminton_shop_api.repositories.ColorRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductVariantRepository;
import com.lezh1n.goodminton_shop_api.services.ColorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ColorServiceImpl implements ColorService {

    private final ColorRepository colorRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ColorMapper colorMapper;

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ColorResponse createColor(ColorRequest request) {
        Color color = colorMapper.toColor(request);

        return colorMapper.toColorResponse(colorRepository.save(color));
    }

    @Override
    public ColorResponse getColorById(Integer colorId) {
        Color color = colorRepository.findById(colorId).orElseThrow(() -> new AppException(ErrorCode.COLOR_NOT_FOUND));

        return colorMapper.toColorResponse(color);
    }

    @Override
    public List<ColorResponse> getAllColors() {
        return colorRepository.findAll().stream().map(colorMapper::toColorResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ColorResponse updateColor(Integer colorId, ColorRequest request) {
        Color color = colorRepository.findById(colorId).orElseThrow(() -> new AppException(ErrorCode.COLOR_NOT_FOUND));

        color.setName(request.getName());

        return colorMapper.toColorResponse(colorRepository.save(color));
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void deleteColor(Integer colorId) {
        Color color = colorRepository.findById(colorId).orElseThrow(() -> new AppException(ErrorCode.COLOR_NOT_FOUND));

        if (productVariantRepository.existByColorId(colorId)) {
            throw new AppException(ErrorCode.COLOR_VARIANT_EXISTED);
        }

        colorRepository.delete(color);
    }

}
