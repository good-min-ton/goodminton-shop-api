package com.lezh1n.goodminton_shop_api.services;

import org.springframework.web.multipart.MultipartFile;

import com.lezh1n.goodminton_shop_api.enums.ResourceType;

public interface CloudinaryService {

    UploadedFile storeFile(MultipartFile file, String folderName);

    void deleteFile(String publicId);

    record UploadedFile(String publicId, String url, ResourceType type) {
    }
}
