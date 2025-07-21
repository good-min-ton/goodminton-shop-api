package com.lezh1n.goodminton_shop_api.services;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    public String storeFile(MultipartFile file, String folderName);
    public void deleteFile(String imageUrl);
}
