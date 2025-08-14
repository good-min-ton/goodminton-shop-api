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
import org.springframework.web.bind.annotation.RestController;

import com.lezh1n.goodminton_shop_api.dtos.ApiResponse;
import com.lezh1n.goodminton_shop_api.dtos.request.DiscountRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductSpecificationRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductVariantRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ReviewRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.DiscountResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductByAttributeResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductSpecificationResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductVariantResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ReviewResponse;
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
	public ApiResponse<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
		return ApiResponse.<ProductResponse>builder()
				.result(productService.createProduct(request))
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
			@Valid @RequestBody ProductRequest request) {
		return ApiResponse.<ProductResponse>builder()
				.result(productService.updateProduct(productId, request))
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

	// Specifications
	@PostMapping("/{productId}/specifications")
	public ApiResponse<ProductSpecificationResponse> addSpecificationToProduct(
			@PathVariable @Min(value = 1, message = "Product ID must be greater than 0") Integer productId,
			@Valid @RequestBody ProductSpecificationRequest request) {
		return ApiResponse.<ProductSpecificationResponse>builder()
				.result(productService.addSpecificationToProduct(productId, request))
				.build();
	}

	@DeleteMapping("/{productId}/specifications/{specId}")
	public ApiResponse<String> deleteSpecification(
			@PathVariable @Min(value = 1, message = "Product ID must be greater than 0") Integer productId,
			@PathVariable @Min(value = 1, message = "Specification ID must be greater than 0") Integer specId) {
		productService.deleteSpecification(productId, specId);
		return ApiResponse.<String>builder()
				.result("Xóa thành công thông số sản phẩm")
				.build();
	}

	// Variants
	@PostMapping("{productId}/variants")
	public ApiResponse<ProductVariantResponse> addVariantToProduct(
			@PathVariable @Min(value = 1, message = "Product IDs must be greater than 0") Integer productId,
			@Valid @RequestBody ProductVariantRequest request) {
		return ApiResponse.<ProductVariantResponse>builder()
				.result(productService.addVariantToProduct(productId, request))
				.build();
	}

	@DeleteMapping("/{productId}/variants/{variantId}")
	public ApiResponse<String> deleteVariant(
			@PathVariable @Min(value = 1, message = "Product ID must be greater than 0") Integer productId,
			@PathVariable @Min(value = 1, message = "Variant ID must be greater than 0") Integer variantId) {
		productService.deleteVariant(productId, variantId);
		return ApiResponse.<String>builder()
				.result("Xóa thành công bản thể sản phẩm")
				.build();
	}

	// Get variant by attributes
	@GetMapping("/{productId}/variants")
	public ApiResponse<ProductByAttributeResponse> getProductByAttribute(
			@PathVariable @Min(value = 1, message = "Product ID must be greater than 0") Integer productId,
			@RequestParam("versionId") Integer versionId,
			@RequestParam("colorId") Integer colorId,
			@RequestParam("sizeId") Integer sizeId) {
		return ApiResponse.<ProductByAttributeResponse>builder()
				.result(productService.getProductByAttributes(productId, versionId, colorId, sizeId))
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
	@PostMapping("/{productId}/review")
	public ApiResponse<ReviewResponse> createReview(@PathVariable Integer productId,
			@Valid @RequestBody ReviewRequest request) {
		return ApiResponse.<ReviewResponse>builder()
				.result(productService.createReview(productId, request))
				.build();
	}
}
