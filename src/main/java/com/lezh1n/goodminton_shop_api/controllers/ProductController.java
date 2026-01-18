package com.lezh1n.goodminton_shop_api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.lezh1n.goodminton_shop_api.dtos.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.request.DiscountRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ReviewRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.DiscountResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ReviewResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.VariantByAttributeResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.VariantImageResponse;
import com.lezh1n.goodminton_shop_api.services.ProductService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	// Products
	@PostMapping
	public ApiResponse<ProductResponse> createProduct(
			@Valid @RequestPart("productInfo") ProductRequest request,
			@RequestPart("productThumbnail") MultipartFile thumbnail) {
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
			@RequestParam(defaultValue = "createAt") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDir) {

		Page<ProductResponse> productPage = productService.getAllProducts(page, size, sortBy, sortDir);

		return ApiResponse.<Page<ProductResponse>>builder()
				.result(productPage)
				.build();
	}

	@PutMapping("/{productId}")
	public ApiResponse<ProductResponse> updateProduct(
			@PathVariable @Min(value = 1, message = "Product ID must be greater than 0") Integer productId,
			@Valid @RequestPart("productInfo") ProductRequest request,
			@RequestPart("productThumbnail") MultipartFile thumbnail) {
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

	// Get variant by attributes
	@GetMapping("/{productId}/variants")
	public ApiResponse<VariantByAttributeResponse> getProductByAttribute(
			@PathVariable @Min(value = 1, message = "Product ID must be greater than 0") Integer productId,
			@RequestParam("versionId") Integer versionId,
			@RequestParam("colorId") Integer colorId,
			@RequestParam("sizeId") Integer sizeId) {
		return ApiResponse.<VariantByAttributeResponse>builder()
				.result(productService.getVariantByAttributes(productId, versionId, colorId, sizeId))
				.build();
	}

	// Product discount
	@PostMapping("/discount/{variantSizeId}")
	public ApiResponse<DiscountResponse> createDiscount(@PathVariable Integer variantSizeId,
			@Valid @RequestBody DiscountRequest request) {
		return ApiResponse.<DiscountResponse>builder()
				.result(productService.createDiscount(variantSizeId, request))
				.build();
	}

	// Product reviews
	@PostMapping("/{productId}/reviews")
	public ApiResponse<ReviewResponse> createReview(@PathVariable Integer productId,
			@Valid @RequestBody ReviewRequest request) {
		return ApiResponse.<ReviewResponse>builder()
				.result(productService.createReview(productId, request))
				.build();
	}

	@GetMapping("/{productId}/reviews")
	public ApiResponse<Page<ReviewResponse>> getReviewsOfProduct(
			@PathVariable Integer productId,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createAt") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDir) {
		return ApiResponse.<Page<ReviewResponse>>builder()
				.result(productService.getReviewsOfProduct(productId, page, size, sortBy, sortDir))
				.build();
	}

	@PostMapping("/{variantId}/images")
	public ApiResponse<VariantImageResponse> uploadVariantImage(@PathVariable Integer variantId,
			@RequestParam("file") MultipartFile file) {
		return ApiResponse.<VariantImageResponse>builder()
				.result(productService.uploadVariantImage(variantId, file))
				.build();
	}

	@DeleteMapping("/{variantId}/images")
	public ApiResponse<String> deleteVariantImage(@PathVariable Integer variantId) {
		productService.deleteVariantImage(variantId);
		return ApiResponse.<String>builder()
				.result("Delete image successfully")
				.build();
	}
}
