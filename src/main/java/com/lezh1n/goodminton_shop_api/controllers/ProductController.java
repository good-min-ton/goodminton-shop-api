package com.lezh1n.goodminton_shop_api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.lezh1n.goodminton_shop_api.common.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ResourceResponse;
import com.lezh1n.goodminton_shop_api.services.ProductService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ApiResponse<ProductResponse> createProduct(
            @Valid @RequestPart("productInfo") ProductRequest request,
            @RequestPart(value = "productThumbnail", required = false) MultipartFile thumbnail) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.createProduct(request, thumbnail))
                .build();
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable Integer productId) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.getProductById(productId))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ApiResponse.<Page<ProductResponse>>builder()
                .result(productService.getAllProducts(page, size, sortBy, sortDir))
                .build();
    }

    @PutMapping("/{productId}")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable @Min(value = 1, message = "Product ID must be greater than 0") Integer productId,
            @Valid @RequestPart("productInfo") ProductRequest request,
            @RequestPart(value = "productThumbnail", required = false) MultipartFile thumbnail) {
        return ApiResponse.<ProductResponse>builder()
                .result(productService.updateProduct(productId, request, thumbnail))
                .build();
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<String> deleteProduct(
            @PathVariable @Min(value = 1, message = "Product ID must be greater than 0") Integer productId) {
        productService.deleteProduct(productId);
        return ApiResponse.<String>builder()
                .result("Xóa sản phẩm thành công")
                .build();
    }

    @PostMapping("/{productId}/images")
    public ApiResponse<ResourceResponse> uploadProductImage(@PathVariable Integer productId,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.<ResourceResponse>builder()
                .result(productService.uploadProductImage(productId, file))
                .build();
    }

    @DeleteMapping("/images/{imageId}")
    public ApiResponse<String> deleteProductImage(@PathVariable Integer imageId) {
        productService.deleteProductImage(imageId);
        return ApiResponse.<String>builder()
                .result("Xoá ảnh thành công")
                .build();
    }
}
