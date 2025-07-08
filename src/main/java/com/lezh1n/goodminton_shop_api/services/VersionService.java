package com.lezh1n.goodminton_shop_api.services;

import java.util.List;

import com.lezh1n.goodminton_shop_api.dtos.request.VersionRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.VersionResponse;

public interface VersionService {
    VersionResponse createVersion(VersionRequest request);

    VersionResponse getVersionById(Integer versionId);

    List<VersionResponse> getAllVersions();

    VersionResponse updateVersion(Integer versionId, VersionRequest request);
}
