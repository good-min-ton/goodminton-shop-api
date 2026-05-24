package com.lezh1n.goodminton_shop_api.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.context.ApplicationEventPublisher;

import com.lezh1n.goodminton_shop_api.configurations.CacheConfig;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductSpecificationRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductVariantRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ResourceResponse;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.entities.ProductVariant;
import com.lezh1n.goodminton_shop_api.enums.ResourceOwner;
import com.lezh1n.goodminton_shop_api.events.ProductChangedEvent;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.ProductMapper;
import com.lezh1n.goodminton_shop_api.mappers.ProductSpecificationMapper;
import com.lezh1n.goodminton_shop_api.mappers.ProductVariantMapper;
import com.lezh1n.goodminton_shop_api.repositories.InventoryRepository;
import com.lezh1n.goodminton_shop_api.repositories.OrderItemRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductSpecificationRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductVariantRepository;
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
    private final InventoryRepository inventoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductMapper productMapper;
    private final ProductVariantMapper productVariantMapper;
    private final ProductSpecificationMapper productSpecificationMapper;
    private final ResourceService resourceService;
    private final ApplicationEventPublisher events;

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

        events.publishEvent(ProductChangedEvent.created(saved.getId()));
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

        events.publishEvent(ProductChangedEvent.updated(
                product.getId(),
                Set.of("name", "description", "specs", "brand", "category")));
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

        // Images now live at product-level under PRODUCT_THUMBNAIL (sort_order=0 is
        // the primary thumbnail; sort_order>0 are gallery images). One owner_id
        // bucket per product, so a single deleteByOwner removes everything.
        resourceService.deleteByOwner(ResourceOwner.PRODUCT_THUMBNAIL, productId);
        productRepository.delete(product);

        events.publishEvent(ProductChangedEvent.deleted(productId));
    }

    @Override
    public ResourceResponse uploadProductImage(Integer productId, MultipartFile file) {
        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return resourceService.upload(ResourceOwner.PRODUCT_THUMBNAIL, productId, file);
    }

    @Override
    public void deleteProductImage(Integer imageId) {
        resourceService.delete(imageId);
    }

    private ProductResponse buildProductResponse(Product product) {
        List<ResourceResponse> images = resourceService
                .listByOwner(ResourceOwner.PRODUCT_THUMBNAIL, product.getId());
        ResourceResponse thumbnail = images.isEmpty() ? null : images.get(0);

        ProductResponse response = productMapper.toProductResponse(product, thumbnail, images);
        response.setSpecifications(product.getSpecifications().stream()
                .map(productSpecificationMapper::toSpecificationResponse)
                .toList());
        response.setVariants(product.getVariants().stream()
                .map(productVariantMapper::toProductVariantResponse)
                .toList());
        return response;
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

    /**
     * Smart diff: variants with id → update; without id → create; existing not in
     * request → delete (only if no inventory/order_item references).
     */
    private void updateVariants(Product product, List<ProductVariantRequest> requests) {
        List<ProductVariant> existing = new ArrayList<>(product.getVariants());
        Map<Integer, ProductVariant> existingById = new HashMap<>();
        existing.forEach(v -> existingById.put(v.getId(), v));

        Set<Integer> requestedIds = new HashSet<>();
        requests.forEach(r -> {
            if (r.getId() != null) {
                requestedIds.add(r.getId());
            }
        });

        // 1. Delete variants no longer in request.
        // Images live at product-level now, no variant-image cleanup needed.
        for (ProductVariant v : existing) {
            if (!requestedIds.contains(v.getId())) {
                ensureVariantNotInUse(v.getId());
                product.getVariants().remove(v);
                productVariantRepository.delete(v);
            }
        }
        productVariantRepository.flush();

        // 2. Update existing or create new.
        for (ProductVariantRequest req : requests) {
            if (req.getId() != null) {
                ProductVariant target = existingById.get(req.getId());
                if (target == null || !Objects.equals(target.getProduct().getId(), product.getId())) {
                    throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
                }
                productVariantMapper.applyUpdate(target, req);
            } else {
                product.getVariants().add(productVariantMapper.toProductVariant(product, req));
            }
        }

        product.setUpdatedAt(LocalDateTime.now());
    }

    private void ensureVariantNotInUse(Integer variantId) {
        if (inventoryRepository.existsByVariant_Id(variantId)
                || orderItemRepository.existsByVariant_Id(variantId)) {
            throw new AppException(ErrorCode.VARIANT_IN_USE);
        }
    }
}
