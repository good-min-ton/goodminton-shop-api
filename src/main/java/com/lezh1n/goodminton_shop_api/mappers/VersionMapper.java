package com.lezh1n.goodminton_shop_api.mappers;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.request.VersionRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.VersionResponse;
import com.lezh1n.goodminton_shop_api.entities.Version;

@Component
public class VersionMapper {
    public Version toVersion(VersionRequest request) {
        return Version.builder()
                .name(request.getName())
                .build();
    }

    public VersionResponse toVersionResponse(Version version) {
        return VersionResponse.builder()
                .versionId(version.getVersionId())
                .name(version.getName())
                .build();
    }
}
