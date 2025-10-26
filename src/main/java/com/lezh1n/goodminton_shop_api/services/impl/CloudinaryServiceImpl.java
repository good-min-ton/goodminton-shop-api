package com.lezh1n.goodminton_shop_api.services.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.services.CloudinaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;
    private static final List<String> ALLOWED_TYPES = List.of("raw", "image", "video");

    @Override
    public CloudinaryFileInfo storeFile(MultipartFile file, String folderName) {
        try {
            if (file.isEmpty()) {
                throw new AppException(ErrorCode.FILE_EMPTY);
            }

            String contentType = file.getContentType();
            String resourceType = detectResourceType(contentType);

            if (contentType == null || !ALLOWED_TYPES.contains(resourceType)) {
                throw new AppException(ErrorCode.FILE_TYPE_NOT_SUPPORTED);
            }

            String publicId = UUID.randomUUID().toString();

            @SuppressWarnings("unchecked")
            Map<String, Object> params = ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", "bot-cv/" + folderName,
                    "overwrite", true);

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            String secureUrl = (String) uploadResult.get("secure_url");
            String uploadedPublicId = (String) uploadResult.get("public_id");

            return new CloudinaryFileInfo(secureUrl, uploadedPublicId);
        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public void deleteFile(String publicId) {
        if (publicId == null || publicId.isEmpty())
            return;

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.warn("Failed to delete image with publicId {}: {}", publicId, e.getMessage(), e);
        }
    }

    // Private methods
    private String detectResourceType(String contentType) {
        if (contentType == null)
            return "raw";
        if (contentType.startsWith("image/"))
            return "image";
        if (contentType.startsWith("video/"))
            return "video";
        return "raw";
    }

    public record CloudinaryFileInfo(String url, String publicId) {
    }
}
