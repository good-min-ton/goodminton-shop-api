package com.lezh1n.goodminton_shop_api.services.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.request.ProductRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductSpecificationRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductVariantRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.VariantImageRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.VariantSizeRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductVariantResponse;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.entities.ProductSpecification;
import com.lezh1n.goodminton_shop_api.entities.ProductVariant;
import com.lezh1n.goodminton_shop_api.entities.VariantImage;
import com.lezh1n.goodminton_shop_api.entities.VariantSize;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.ProductMapper;
import com.lezh1n.goodminton_shop_api.mappers.ProductSpecificationMapper;
import com.lezh1n.goodminton_shop_api.mappers.ProductVariantMapper;
import com.lezh1n.goodminton_shop_api.mappers.VariantImageMapper;
import com.lezh1n.goodminton_shop_api.mappers.VariantSizeMapper;
import com.lezh1n.goodminton_shop_api.repositories.ProductRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductSpecificationRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductVariantRepository;
import com.lezh1n.goodminton_shop_api.repositories.VariantImageRepository;
import com.lezh1n.goodminton_shop_api.repositories.VariantSizeRepository;
import com.lezh1n.goodminton_shop_api.services.ProductService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductSpecificationRepository productSpecificationRepository;
    private final VariantSizeRepository variantSizeRepository;
    private final VariantImageRepository variantImageRepository;
    private final ProductMapper productMapper;
    private final ProductVariantMapper productVariantMapper;
    private final ProductSpecificationMapper productSpecificationMapper;
    private final VariantSizeMapper variantSizeMapper;
    private final VariantImageMapper variantImageMapper;

    /* -- Public methods -- */
    // Product CRUD
    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = productMapper.toProduct(request);

        Product savedProduct = productRepository.save(product);

        createSpecifications(savedProduct, request.getSpecifications());

        request.getVariants().forEach(variantReq -> createVariant(savedProduct, variantReq));

        return getProductById(savedProduct.getProductId());
    }

    @Override
    public ProductResponse getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductResponse productResponse = productMapper.toProductResponse(product);

        productResponse.setSpecifications(
                product.getSpecifications().stream().map(productSpecificationMapper::toSpecificationResponse).toList());

        List<ProductVariantResponse> variantResponses = product.getVariants().stream()
                .map(variant -> {
                    ProductVariantResponse vr = productVariantMapper.toProductVariantResponse(variant);

                    vr.setSizes(variant.getSizes().stream().map(variantSizeMapper::toVariantSizeResponse).toList());

                    vr.setImages(variant.getImages().stream().map(variantImageMapper::toVariantImageResponse).toList());

                    return vr;
                })
                .toList();

        productResponse.setVariants(variantResponses);
        return productResponse;
    }

    @Override
    public Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Product> productPage = productRepository.findAll(pageable);

        return productPage.map(product -> ProductResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .thumbnailUrl(product.getThumbnailUrl())
                .createAt(product.getCreateAt())
                .build());
    }

    // Variant CRUD
    @Override
    public ProductVariantResponse addVariantToProduct(Integer productId, ProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductVariant variant = productVariantMapper.toProductVariant(product, request);
        variant = productVariantRepository.save(variant);

        product.getVariants().add(variant);
        createVariantSizes(variant, request.getSizes());
        createVariantImages(variant, request.getImages());

        return productVariantMapper.toProductVariantResponse(variant);
    }

    @Override
    public ProductVariantResponse updateVariant(Integer variantId, ProductVariantRequest request) {

    }

    /* -- Private methods-- */
    // Product CRUD
    private void createSpecifications(Product product, List<ProductSpecificationRequest> specs) {
        List<ProductSpecification> specifications = specs.stream()
                .map(s -> productSpecificationMapper.toProductSpecification(product, s))
                .toList();
        product.getSpecifications().addAll(specifications);
        productSpecificationRepository.saveAll(specifications);
    }

    private void createVariant(Product product, ProductVariantRequest request) {
        ProductVariant variant = productVariantMapper.toProductVariant(product, request);
        variant = productVariantRepository.save(variant);

        product.getVariants().add(variant);
        createVariantSizes(variant, request.getSizes());
        createVariantImages(variant, request.getImages());
    }

    private void createVariantSizes(ProductVariant variant, List<VariantSizeRequest> requests) {
        List<VariantSize> sizes = requests.stream()
                .map(s -> variantSizeMapper.toVariantSize(variant, s))
                .toList();
        variant.getSizes().addAll(sizes);
        variantSizeRepository.saveAll(sizes);
    }

    private void createVariantImages(ProductVariant variant, List<VariantImageRequest> requests) {
        List<VariantImage> images = requests.stream()
                .map(i -> variantImageMapper.toVariantImage(variant, i))
                .toList();
        variant.getImages().addAll(images);
        variantImageRepository.saveAll(images);
    }

    // Variant CRUD
    private void updateVariantSize(ProductVariant variant, List<VariantSizeRequest> requests) {
        variantSizeRepository.deleteAll(variant.getSizes());
        variant.getSizes().clear();

        List<VariantSize> sizes = requests.stream()
                .map(s -> variantSizeMapper.toVariantSize(variant, s))
                .toList();
        variantSizeRepository.saveAll(sizes);
        variant.getSizes().addAll(sizes);
    }

    private void updateVariantImage(ProductVariant variant, List<VariantImageRequest> requests) {
        
    }
}
