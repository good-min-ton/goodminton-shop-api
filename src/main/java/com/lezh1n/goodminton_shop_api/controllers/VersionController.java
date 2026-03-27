package com.lezh1n.goodminton_shop_api.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.common.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.request.VersionRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.VersionResponse;
import com.lezh1n.goodminton_shop_api.services.VersionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/versions")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    @PostMapping
    public ApiResponse<VersionResponse> createVersion(@Valid @RequestBody VersionRequest request) {
        return ApiResponse.<VersionResponse>builder()
                .result(versionService.createVersion(request))
                .build();
    }

    @GetMapping("/{versionId}")
    public ApiResponse<VersionResponse> getVersionById(@PathVariable Integer versionId) {
        return ApiResponse.<VersionResponse>builder()
                .result(versionService.getVersionById(versionId))
                .build();
    }

    @GetMapping
    public ApiResponse<List<VersionResponse>> getAllVersions() {
        return ApiResponse.<List<VersionResponse>>builder()
                .result(versionService.getAllVersions())
                .build();
    }

    @PutMapping("/{versionId}")
    public ApiResponse<VersionResponse> updateVersion(
            @PathVariable Integer versionId,
            @Valid @RequestBody VersionRequest request) {
        return ApiResponse.<VersionResponse>builder()
                .result(versionService.updateVersion(versionId, request))
                .build();
    }

    @DeleteMapping("/{versionId}")
    public ApiResponse<String> deleteVersion(@PathVariable Integer versionId) {
        versionService.deleteVersion(versionId);
        return ApiResponse.<String>builder()
                .result("Xoá phiên bản thành công")
                .build();
    }
}
