package com.lezh1n.goodminton_shop_api.services;

import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import com.lezh1n.goodminton_shop_api.dtos.response.ResourceResponse;
import com.lezh1n.goodminton_shop_api.enums.ResourceOwner;

public interface ResourceService {

    ResourceResponse upload(ResourceOwner ownerType, Integer ownerId, MultipartFile file);

    List<ResourceResponse> uploadBatch(ResourceOwner ownerType, Integer ownerId, List<MultipartFile> files);

    ResourceResponse replaceSingle(ResourceOwner ownerType, Integer ownerId, MultipartFile file);

    void delete(Integer resourceId);

    void deleteByOwner(ResourceOwner ownerType, Integer ownerId);

    List<ResourceResponse> reorder(ResourceOwner ownerType, Integer ownerId, List<Integer> resourceIdsInOrder);

    List<ResourceResponse> listByOwner(ResourceOwner ownerType, Integer ownerId);

    Optional<ResourceResponse> findSingle(ResourceOwner ownerType, Integer ownerId);
}
