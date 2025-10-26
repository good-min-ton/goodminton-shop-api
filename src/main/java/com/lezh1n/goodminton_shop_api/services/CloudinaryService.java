package com.lezh1n.goodminton_shop_api.services;

import org.springframework.web.multipart.MultipartFile;

import com.lezh1n.goodminton_shop_api.services.impl.CloudinaryServiceImpl.CloudinaryFileInfo;

public interface CloudinaryService {
    public CloudinaryFileInfo storeFile(MultipartFile file, String folderName);

    public void deleteFile(String imageUrl);
}
