package com.lezh1n.goodminton_shop_api.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
import com.lezh1n.goodminton_shop_api.entities.Resources;
import com.lezh1n.goodminton_shop_api.entities.Review;
import com.lezh1n.goodminton_shop_api.enums.ResourceOwner;
import com.lezh1n.goodminton_shop_api.enums.ResourceType;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.ProductMapper;
import com.lezh1n.goodminton_shop_api.mappers.ProductSpecificationMapper;
import com.lezh1n.goodminton_shop_api.mappers.ProductVariantMapper;
import com.lezh1n.goodminton_shop_api.mappers.ResourceMapper;
import com.lezh1n.goodminton_shop_api.mappers.ReviewMapper;
import com.lezh1n.goodminton_shop_api.repositories.OrderItemRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductSpecificationRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductVariantRepository;
import com.lezh1n.goodminton_shop_api.repositories.ResourceRepository;
import com.lezh1n.goodminton_shop_api.repositories.ReviewRepository;
import com.lezh1n.goodminton_shop_api.security.CurrentAccountProvider;
import com.lezh1n.goodminton_shop_api.services.CloudinaryService;
import com.lezh1n.goodminton_shop_api.services.ProductService;
import com.lezh1n.goodminton_shop_api.services.impl.CloudinaryServiceImpl.CloudinaryFileInfo;

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
    private final ResourceRepository resourceRepository;
    private final OrderItemRepository orderItemRepository;

    private final ProductMapper productMapper;
    private final ProductVariantMapper productVariantMapper;
    private final ProductSpecificationMapper productSpecificationMapper;
    private final ResourceMapper resourceMapper;
    private final ReviewMapper reviewMapper;

    private final CurrentAccountProvider currentAccountProvider;
    private final CloudinaryService cloudinaryService;

    @Override
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
            uploadThumbnail(saved.getId(), thumbnail);
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
            replaceThumbnail(product.getId(), thumbnail);
        }

        return buildProductResponse(product);
    }

    @Override
    public void deleteProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (productRepository.existsByRelatedProduct_Id(productId)) {
            throw new AppException(ErrorCode.PRODUCT_HAS_RELATED_CHILDREN);
        }

        deleteAllResourcesOfProduct(product);
        productRepository.delete(product);
    }

    @Override
    public ResourceResponse uploadVariantImage(Integer variantId, MultipartFile file) {
        if (!productVariantRepository.existsById(variantId)) {
            throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
        }
        CloudinaryFileInfo info = cloudinaryService.storeFile(file, "variant_image");
        int nextOrder = resourceRepository
                .findTopByOwnerTypeAndOwnerIdOrderBySortOrderDesc(ResourceOwner.VARIANT_IMAGE, variantId)
                .map(r -> r.getSortOrder() + 1)
                .orElse(0);
        Resources saved = resourceRepository.save(resourceMapper.toResource(
                ResourceOwner.VARIANT_IMAGE, variantId, info, ResourceType.IMAGE, nextOrder));
        return resourceMapper.toResourceResponse(saved);
    }

    @Override
    public void deleteVariantImage(Integer imageId) {
        Resources image = resourceRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_IMAGE_NOT_FOUND));
        cloudinaryService.deleteFile(image.getPublicId());
        resourceRepository.delete(image);
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
        ResourceResponse thumbnail = resourceRepository
                .findFirstByOwnerTypeAndOwnerIdOrderBySortOrderAsc(ResourceOwner.PRODUCT_THUMBNAIL, product.getId())
                .map(resourceMapper::toResourceResponse)
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
        List<ResourceResponse> images = resourceRepository
                .findByOwnerTypeAndOwnerIdOrderBySortOrderAsc(ResourceOwner.VARIANT_IMAGE, variant.getId())
                .stream()
                .map(resourceMapper::toResourceResponse)
                .toList();
        return productVariantMapper.toProductVariantResponse(variant, images);
    }

    private void uploadThumbnail(Integer productId, MultipartFile file) {
        CloudinaryFileInfo info = cloudinaryService.storeFile(file, "product_thumbnail");
        resourceRepository.save(resourceMapper.toResource(
                ResourceOwner.PRODUCT_THUMBNAIL, productId, info, ResourceType.IMAGE, 0));
    }

    private void replaceThumbnail(Integer productId, MultipartFile file) {
        resourceRepository.findFirstByOwnerTypeAndOwnerIdOrderBySortOrderAsc(
                ResourceOwner.PRODUCT_THUMBNAIL, productId)
                .ifPresent(existing -> {
                    cloudinaryService.deleteFile(existing.getPublicId());
                    resourceRepository.delete(existing);
                });
        uploadThumbnail(productId, file);
    }

    private void deleteAllResourcesOfProduct(Product product) {
        deleteResources(ResourceOwner.PRODUCT_THUMBNAIL, product.getId());
        product.getVariants().forEach(v -> deleteResources(ResourceOwner.VARIANT_IMAGE, v.getId()));
    }

    private void deleteResources(ResourceOwner ownerType, Integer ownerId) {
        resourceRepository.findByOwnerTypeAndOwnerIdOrderBySortOrderAsc(ownerType, ownerId)
                .forEach(r -> {
                    cloudinaryService.deleteFile(r.getPublicId());
                    resourceRepository.delete(r);
                });
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
        existing.forEach(v -> deleteResources(ResourceOwner.VARIANT_IMAGE, v.getId()));
        productVariantRepository.deleteByProduct_Id(product.getId());
        product.getVariants().clear();
        productVariantRepository.flush();
        requests.forEach(r -> product.getVariants()
                .add(productVariantMapper.toProductVariant(product, r)));
        product.setUpdatedAt(LocalDateTime.now());
    }
}
