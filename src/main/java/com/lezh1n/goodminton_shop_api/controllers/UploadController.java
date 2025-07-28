package com.lezh1n.goodminton_shop_api.controllers;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.lezh1n.goodminton_shop_api.dtos.ApiResponse;
import com.lezh1n.goodminton_shop_api.services.impl.CloudinaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/images")
    public ApiResponse<Map<String, String>> uploadImages(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "products") String folder) {
        Map<String, String> result = cloudinaryService.uploadImage(file, folder);
        return ApiResponse.<Map<String, String>>builder()
                .result(result)
                .build();
    }
}
