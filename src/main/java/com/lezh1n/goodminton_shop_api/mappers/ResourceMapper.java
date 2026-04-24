package com.lezh1n.goodminton_shop_api.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.lezh1n.goodminton_shop_api.dtos.response.ResourceResponse;
import com.lezh1n.goodminton_shop_api.entities.Resources;
import com.lezh1n.goodminton_shop_api.enums.ResourceOwner;
import com.lezh1n.goodminton_shop_api.enums.ResourceType;
import com.lezh1n.goodminton_shop_api.services.impl.CloudinaryServiceImpl.CloudinaryFileInfo;

@Component
public class ResourceMapper {

    public Resources toResource(ResourceOwner ownerType, Integer ownerId, CloudinaryFileInfo fileInfo,
            ResourceType type, int sortOrder) {
        return Resources.builder()
                .ownerType(ownerType)
                .ownerId(ownerId)
                .publicId(fileInfo.publicId())
                .url(fileInfo.url())
                .type(type)
                .sortOrder(sortOrder)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public ResourceResponse toResourceResponse(Resources resource) {
        return ResourceResponse.builder()
                .id(resource.getId())
                .publicId(resource.getPublicId())
                .url(resource.getUrl())
                .type(resource.getType())
                .sortOrder(resource.getSortOrder())
                .createdAt(resource.getCreatedAt())
                .build();
    }
}
