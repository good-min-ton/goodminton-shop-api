package com.lezh1n.goodminton_shop_api.services.impl;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.request.VersionRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.VersionResponse;
import com.lezh1n.goodminton_shop_api.entities.Version;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.VersionMapper;
import com.lezh1n.goodminton_shop_api.repositories.VersionRepository;
import com.lezh1n.goodminton_shop_api.services.VersionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {

    private final VersionRepository versionRepository;
    private final VersionMapper versionMapper;

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public VersionResponse createVersion(VersionRequest request) {

        Version version = versionMapper.toVersion(request);

        return versionMapper.toVersionResponse(versionRepository.save(version));
    }

    @Override
    public VersionResponse getVersionById(Integer versionId) {
        Version version = versionRepository.findById(versionId)
                .orElseThrow(() -> new AppException(ErrorCode.VERSION_NOT_FOUND));

        return versionMapper.toVersionResponse(version);
    }

    @Override
    public List<VersionResponse> getAllVersions() {
        return versionRepository.findAll().stream().map(versionMapper::toVersionResponse).toList();
    }

    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public VersionResponse updateVersion(Integer versionId, VersionRequest request) {
        Version version = versionRepository.findById(versionId)
                .orElseThrow(() -> new AppException(ErrorCode.VERSION_NOT_FOUND));

        version.setName(request.getName());

        return versionMapper.toVersionResponse(version);
    }

}
