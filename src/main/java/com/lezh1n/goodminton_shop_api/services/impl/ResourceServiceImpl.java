package com.lezh1n.goodminton_shop_api.services.impl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.lezh1n.goodminton_shop_api.dtos.response.ResourceResponse;
import com.lezh1n.goodminton_shop_api.entities.Resources;
import com.lezh1n.goodminton_shop_api.enums.ResourceOwner;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.ResourceMapper;
import com.lezh1n.goodminton_shop_api.repositories.ResourceRepository;
import com.lezh1n.goodminton_shop_api.services.CloudinaryService;
import com.lezh1n.goodminton_shop_api.services.CloudinaryService.UploadedFile;
import com.lezh1n.goodminton_shop_api.services.ResourceService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private static final Map<ResourceOwner, String> FOLDERS = new EnumMap<>(ResourceOwner.class);
    static {
        FOLDERS.put(ResourceOwner.PRODUCT_THUMBNAIL, "products/thumbnails");
        FOLDERS.put(ResourceOwner.VARIANT_IMAGE, "products/variants");
        FOLDERS.put(ResourceOwner.REVIEW_MEDIA, "reviews");
    }

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public ResourceResponse upload(ResourceOwner ownerType, Integer ownerId, MultipartFile file) {
        UploadedFile uploaded = cloudinaryService.storeFile(file, folderFor(ownerType));
        int sortOrder = nextSortOrder(ownerType, ownerId);
        try {
            Resources saved = resourceRepository.save(resourceMapper.toResource(
                    ownerType, ownerId, uploaded.publicId(), uploaded.url(), uploaded.type(), sortOrder));
            return resourceMapper.toResourceResponse(saved);
        } catch (RuntimeException e) {
            cloudinaryService.deleteFile(uploaded.publicId());
            throw e;
        }
    }

    @Override
    @Transactional
    public List<ResourceResponse> uploadBatch(ResourceOwner ownerType, Integer ownerId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        List<UploadedFile> uploaded = new ArrayList<>(files.size());
        try {
            for (MultipartFile file : files) {
                uploaded.add(cloudinaryService.storeFile(file, folderFor(ownerType)));
            }
        } catch (RuntimeException e) {
            uploaded.forEach(u -> cloudinaryService.deleteFile(u.publicId()));
            throw e;
        }

        int sortOrder = nextSortOrder(ownerType, ownerId);
        List<ResourceResponse> results = new ArrayList<>(uploaded.size());
        try {
            for (UploadedFile u : uploaded) {
                Resources saved = resourceRepository.save(resourceMapper.toResource(
                        ownerType, ownerId, u.publicId(), u.url(), u.type(), sortOrder++));
                results.add(resourceMapper.toResourceResponse(saved));
            }
        } catch (RuntimeException e) {
            uploaded.forEach(u -> cloudinaryService.deleteFile(u.publicId()));
            throw e;
        }
        return results;
    }

    @Override
    @Transactional
    public ResourceResponse replaceSingle(ResourceOwner ownerType, Integer ownerId, MultipartFile file) {
        UploadedFile uploaded = cloudinaryService.storeFile(file, folderFor(ownerType));

        Optional<Resources> existing = resourceRepository
                .findFirstByOwnerTypeAndOwnerIdOrderBySortOrderAsc(ownerType, ownerId);
        String oldPublicId = existing.map(Resources::getPublicId).orElse(null);

        Resources saved;
        try {
            existing.ifPresent(resourceRepository::delete);
            saved = resourceRepository.save(resourceMapper.toResource(
                    ownerType, ownerId, uploaded.publicId(), uploaded.url(), uploaded.type(), 0));
        } catch (RuntimeException e) {
            cloudinaryService.deleteFile(uploaded.publicId());
            throw e;
        }

        if (oldPublicId != null) {
            afterCommit(() -> cloudinaryService.deleteFile(oldPublicId));
        }
        return resourceMapper.toResourceResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Integer resourceId) {
        Resources resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        String publicId = resource.getPublicId();
        resourceRepository.delete(resource);
        afterCommit(() -> cloudinaryService.deleteFile(publicId));
    }

    @Override
    @Transactional
    public void deleteByOwner(ResourceOwner ownerType, Integer ownerId) {
        List<Resources> resources = resourceRepository
                .findByOwnerTypeAndOwnerIdOrderBySortOrderAsc(ownerType, ownerId);
        if (resources.isEmpty()) {
            return;
        }
        List<String> publicIds = resources.stream().map(Resources::getPublicId).toList();
        resourceRepository.deleteAll(resources);
        afterCommit(() -> publicIds.forEach(cloudinaryService::deleteFile));
    }

    @Override
    @Transactional
    public List<ResourceResponse> reorder(ResourceOwner ownerType, Integer ownerId, List<Integer> resourceIdsInOrder) {
        List<Resources> current = resourceRepository
                .findByOwnerTypeAndOwnerIdOrderBySortOrderAsc(ownerType, ownerId);

        if (resourceIdsInOrder == null || resourceIdsInOrder.size() != current.size()) {
            throw new AppException(ErrorCode.RESOURCE_REORDER_INVALID);
        }
        Set<Integer> currentIds = new HashSet<>();
        current.forEach(r -> currentIds.add(r.getId()));
        if (!currentIds.equals(new HashSet<>(resourceIdsInOrder))) {
            throw new AppException(ErrorCode.RESOURCE_REORDER_INVALID);
        }

        Map<Integer, Resources> byId = new HashMap<>();
        current.forEach(r -> byId.put(r.getId(), r));

        List<Resources> reordered = new ArrayList<>(resourceIdsInOrder.size());
        for (int i = 0; i < resourceIdsInOrder.size(); i++) {
            Resources r = byId.get(resourceIdsInOrder.get(i));
            r.setSortOrder(i);
            reordered.add(r);
        }
        resourceRepository.saveAll(reordered);
        return reordered.stream().map(resourceMapper::toResourceResponse).toList();
    }

    @Override
    public List<ResourceResponse> listByOwner(ResourceOwner ownerType, Integer ownerId) {
        return resourceRepository.findByOwnerTypeAndOwnerIdOrderBySortOrderAsc(ownerType, ownerId)
                .stream()
                .map(resourceMapper::toResourceResponse)
                .toList();
    }

    @Override
    public Optional<ResourceResponse> findSingle(ResourceOwner ownerType, Integer ownerId) {
        return resourceRepository.findFirstByOwnerTypeAndOwnerIdOrderBySortOrderAsc(ownerType, ownerId)
                .map(resourceMapper::toResourceResponse);
    }

    private int nextSortOrder(ResourceOwner ownerType, Integer ownerId) {
        return resourceRepository
                .findTopByOwnerTypeAndOwnerIdOrderBySortOrderDesc(ownerType, ownerId)
                .map(r -> r.getSortOrder() + 1)
                .orElse(0);
    }

    private String folderFor(ResourceOwner ownerType) {
        return FOLDERS.getOrDefault(ownerType, "misc");
    }

    // Run Cloudinary delete after DB commit so a rollback never leaves files orphaned.
    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
