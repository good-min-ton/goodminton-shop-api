package com.lezh1n.goodminton_shop_api.services.impl;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lezh1n.goodminton_shop_api.enums.ResourceType;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.services.CloudinaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private static final String CLOUDINARY_IMAGE = "image";
    private static final String CLOUDINARY_VIDEO = "video";
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 30L * 1024 * 1024;

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder-prefix}")
    private String folderPrefix;

    @Override
    public UploadedFile storeFile(MultipartFile file, String folderName) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        ResourceType type = detectType(file.getContentType());
        validateSize(file.getSize(), type);

        String publicId = UUID.randomUUID().toString();
        Map<String, Object> params = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folderPrefix + "/" + folderName,
                "resource_type", toCloudinaryType(type));

        try {
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);
            return new UploadedFile(
                    (String) result.get("public_id"),
                    (String) result.get("secure_url"),
                    type);
        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void deleteFile(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.warn("Cloudinary delete failed for publicId {}: {}", publicId, e.getMessage());
        }
    }

    private ResourceType detectType(String contentType) {
        if (contentType == null) {
            throw new AppException(ErrorCode.FILE_TYPE_NOT_SUPPORTED);
        }
        if (contentType.startsWith("image/")) {
            return ResourceType.IMAGE;
        }
        if (contentType.startsWith("video/")) {
            return ResourceType.VIDEO;
        }
        throw new AppException(ErrorCode.FILE_TYPE_NOT_SUPPORTED);
    }

    private void validateSize(long fileSize, ResourceType type) {
        long max = type == ResourceType.VIDEO ? MAX_VIDEO_SIZE : MAX_IMAGE_SIZE;
        if (fileSize > max) {
            throw new AppException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    private String toCloudinaryType(ResourceType type) {
        return type == ResourceType.VIDEO ? CLOUDINARY_VIDEO : CLOUDINARY_IMAGE;
    }
}
