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

import com.lezh1n.goodminton_shop_api.dtos.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.request.ColorRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ColorResponse;
import com.lezh1n.goodminton_shop_api.services.ColorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/colors")
@RequiredArgsConstructor
public class ColorController {

    private final ColorService colorService;

    @PostMapping
    public ApiResponse<ColorResponse> createColor(@Valid @RequestBody ColorRequest request) {
        return ApiResponse.<ColorResponse>builder()
                .result(colorService.createColor(request))
                .build();
    }

    @GetMapping("/{colorId}")
    public ApiResponse<ColorResponse> getColorById(@PathVariable Integer colorId) {
        return ApiResponse.<ColorResponse>builder()
                .result(colorService.getColorById(colorId))
                .build();
    }

    @GetMapping
    public ApiResponse<List<ColorResponse>> getAllColors() {
        return ApiResponse.<List<ColorResponse>>builder()
                .result(colorService.getAllColors())
                .build();
    }

    @PutMapping("/{colorId}")
    public ApiResponse<ColorResponse> updateColor(
            @PathVariable Integer colorId,
            @Valid @RequestBody ColorRequest request) {
        return ApiResponse.<ColorResponse>builder()
                .result(colorService.updateColor(colorId, request))
                .build();
    }

    @DeleteMapping("/{colorId}")
    public ApiResponse<String> deleteColor(@PathVariable Integer colorId) {
        colorService.deleteColor(colorId);
        return ApiResponse.<String>builder()
                .result("Xoá màu thành công")
                .build();
    }
}
