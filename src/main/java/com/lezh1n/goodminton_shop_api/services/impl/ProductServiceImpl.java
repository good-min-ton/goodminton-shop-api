package com.lezh1n.goodminton_shop_api.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.lezh1n.goodminton_shop_api.configurations.CacheConfig;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductSpecificationRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductVariantRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ReviewRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductVariantResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ResourceResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ReviewResponse;
import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.entities.OrderItem;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.entities.ProductVariant;
import com.lezh1n.goodminton_shop_api.entities.Review;
import com.lezh1n.goodminton_shop_api.enums.ResourceOwner;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.ProductMapper;
import com.lezh1n.goodminton_shop_api.mappers.ProductSpecificationMapper;
import com.lezh1n.goodminton_shop_api.mappers.ProductVariantMapper;
import com.lezh1n.goodminton_shop_api.mappers.ReviewMapper;
import com.lezh1n.goodminton_shop_api.repositories.OrderItemRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductSpecificationRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductVariantRepository;
import com.lezh1n.goodminton_shop_api.repositories.ReviewRepository;
import com.lezh1n.goodminton_shop_api.security.CurrentAccountProvider;
import com.lezh1n.goodminton_shop_api.services.ProductService;
import com.lezh1n.goodminton_shop_api.services.ResourceService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductSpecificationRepository productSpecificationRepository;
    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;

    private final ProductMapper productMapper;
    private final ProductVariantMapper productVariantMapper;
    private final ProductSpecificationMapper productSpecificationMapper;
    private final ReviewMapper reviewMapper;

    private final ResourceService resourceService;
    private final CurrentAccountProvider currentAccountProvider;

    @Override
    @CacheEvict(value = CacheConfig.RECOMMENDATIONS_CACHE, allEntries = true)
    public ProductResponse createProduct(ProductRequest request, MultipartFile thumbnail) {
        if (productRepository.existsBySlug(request.getSlug())) {
            throw new AppException(ErrorCode.PRODUCT_SLUG_EXISTED);
        }

        Product product = productMapper.toProduct(request);
        Product saved = productRepository.save(product);

        if (request.getSpecifications() != null) {
            request.getSpecifications().forEach(s -> saved.getSpecifications()
                    .add(productSpecificationMapper.toProductSpecification(saved, s)));
        }
        request.getVariants().forEach(v -> saved.getVariants()
                .add(productVariantMapper.toProductVariant(saved, v)));

        productRepository.save(saved);

        if (thumbnail != null && !thumbnail.isEmpty()) {
            resourceService.upload(ResourceOwner.PRODUCT_THUMBNAIL, saved.getId(), thumbnail);
        }

        return buildProductResponse(saved);
    }

    @Override
    public ProductResponse getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return buildProductResponse(product);
    }

    @Override
    public Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);
        return productRepository.findAll(pageable).map(this::buildProductResponse);
    }

    @Override
    @CacheEvict(value = CacheConfig.RECOMMENDATIONS_CACHE, allEntries = true)
    public ProductResponse updateProduct(Integer productId, ProductRequest request, MultipartFile thumbnail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.getSlug().equals(request.getSlug()) && productRepository.existsBySlug(request.getSlug())) {
            throw new AppException(ErrorCode.PRODUCT_SLUG_EXISTED);
        }

        productMapper.updateProduct(product, request);
        updateSpecifications(product, request.getSpecifications());
        updateVariants(product, request.getVariants());
        productRepository.save(product);

        if (thumbnail != null && !thumbnail.isEmpty()) {
            resourceService.replaceSingle(ResourceOwner.PRODUCT_THUMBNAIL, product.getId(), thumbnail);
        }

        return buildProductResponse(product);
    }

    @Override
    @CacheEvict(value = CacheConfig.RECOMMENDATIONS_CACHE, allEntries = true)
    public void deleteProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (productRepository.existsByRelatedProduct_Id(productId)) {
            throw new AppException(ErrorCode.PRODUCT_HAS_RELATED_CHILDREN);
        }

        resourceService.deleteByOwner(ResourceOwner.PRODUCT_THUMBNAIL, productId);
        product.getVariants().forEach(v -> resourceService.deleteByOwner(ResourceOwner.VARIANT_IMAGE, v.getId()));
        productRepository.delete(product);
    }

    @Override
    public ResourceResponse uploadVariantImage(Integer variantId, MultipartFile file) {
        if (!productVariantRepository.existsById(variantId)) {
            throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
        }
        return resourceService.upload(ResourceOwner.VARIANT_IMAGE, variantId, file);
    }

    @Override
    public void deleteVariantImage(Integer imageId) {
        resourceService.delete(imageId);
    }

    @Override
    public ReviewResponse createReview(Integer productId, ReviewRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND));

        if (reviewRepository.existsByOrderItem_Id(orderItem.getId())) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Account customer = currentAccountProvider.getCurrentAccount();
        Review review = reviewMapper.toReview(product, customer, orderItem, request);
        return reviewMapper.toReviewResponse(reviewRepository.save(review));
    }

    @Override
    public Page<ReviewResponse> getReviewsOfProduct(Integer productId, int page, int size, String sortBy,
            String sortDir) {
        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);
        return reviewRepository.findByProduct_Id(productId, pageable).map(reviewMapper::toReviewResponse);
    }

    private ProductResponse buildProductResponse(Product product) {
        ResourceResponse thumbnail = resourceService
                .findSingle(ResourceOwner.PRODUCT_THUMBNAIL, product.getId())
                .orElse(null);

        ProductResponse response = productMapper.toProductResponse(product, thumbnail);
        response.setSpecifications(product.getSpecifications().stream()
                .map(productSpecificationMapper::toSpecificationResponse)
                .toList());
        response.setVariants(product.getVariants().stream()
                .map(this::buildVariantResponse)
                .toList());
        return response;
    }

    private ProductVariantResponse buildVariantResponse(ProductVariant variant) {
        List<ResourceResponse> images = resourceService.listByOwner(ResourceOwner.VARIANT_IMAGE, variant.getId());
        return productVariantMapper.toProductVariantResponse(variant, images);
    }

    private void updateSpecifications(Product product, List<ProductSpecificationRequest> requests) {
        productSpecificationRepository.deleteByProduct_Id(product.getId());
        product.getSpecifications().clear();
        productSpecificationRepository.flush();
        if (requests == null) {
            return;
        }
        requests.forEach(r -> product.getSpecifications()
                .add(productSpecificationMapper.toProductSpecification(product, r)));
    }

    private void updateVariants(Product product, List<ProductVariantRequest> requests) {
        List<ProductVariant> existing = productVariantRepository.findByProduct_Id(product.getId());
        existing.forEach(v -> resourceService.deleteByOwner(ResourceOwner.VARIANT_IMAGE, v.getId()));
        productVariantRepository.deleteByProduct_Id(product.getId());
        product.getVariants().clear();
        productVariantRepository.flush();
        requests.forEach(r -> product.getVariants()
                .add(productVariantMapper.toProductVariant(product, r)));
        product.setUpdatedAt(LocalDateTime.now());
    }
}
