package com.lezh1n.goodminton_shop_api.services;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    public Map<String, String> uploadImage(MultipartFile file, String folder); 
}
