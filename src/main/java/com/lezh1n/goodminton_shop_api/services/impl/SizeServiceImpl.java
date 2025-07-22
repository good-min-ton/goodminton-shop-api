package com.lezh1n.goodminton_shop_api.services.impl;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.request.SizeRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.SizeResponse;
import com.lezh1n.goodminton_shop_api.entities.Size;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.SizeMapper;
import com.lezh1n.goodminton_shop_api.repositories.SizeRepository;
import com.lezh1n.goodminton_shop_api.repositories.VariantSizeRepository;
import com.lezh1n.goodminton_shop_api.services.SizeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SizeServiceImpl implements SizeService {

    private final SizeRepository sizeRepository;
    private final VariantSizeRepository variantSizeRepository;
    private final SizeMapper sizeMapper;

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public SizeResponse createSize(SizeRequest request) {

        Size size = sizeMapper.toSize(request);

        return sizeMapper.toSizeResponse(sizeRepository.save(size));
    }

    @Override
    public SizeResponse getSizeById(Integer sizeId) {
        Size size = sizeRepository.findById(sizeId).orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));

        return sizeMapper.toSizeResponse(size);
    }

    @Override
    public List<SizeResponse> getAllSizes() {
        return sizeRepository.findAll().stream().map(sizeMapper::toSizeResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public SizeResponse updateSize(Integer sizeId, SizeRequest request) {
        Size size = sizeRepository.findById(sizeId).orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));

        size.setName(request.getName());
        size.setType(request.getType());

        return sizeMapper.toSizeResponse(sizeRepository.save(size));
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void deleteSize(Integer sizeId) {
        Size size = sizeRepository.findById(sizeId).orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));

        if (variantSizeRepository.existBySizeId(sizeId)) {
            throw new AppException(ErrorCode.SIZE_VARIANT_EXISTED);
        }

        sizeRepository.delete(size);
    }

}
