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
import com.lezh1n.goodminton_shop_api.dtos.response.ProductSpecificationResponse;
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
        productRepository.save(product);
        return getProductById(savedProduct.getProductId());
    }

    @Override
    public ProductResponse getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductResponse productResponse = productMapper.toProductResponse(product);

        productResponse.setSpecifications(product.getSpecifications().stream()
                .map(productSpecificationMapper::toSpecificationResponse)
                .toList());

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

    @Override
    @Transactional
    public ProductResponse updateProduct(Integer productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        productMapper.updateProduct(product, request);
        updateSpecification(product, request.getSpecifications());
        updateVariant(product, request.getVariants());
        productRepository.save(product);

        return getProductById(productId);
    }

    @Override
    @Transactional
    public void deleteProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        productRepository.delete(product);
    }

    // Specifications CRUD
    @Override
    @Transactional
    public ProductSpecificationResponse addSpecificationToProduct(Integer productId,
            ProductSpecificationRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductSpecification specification = productSpecificationMapper.toProductSpecification(product, request);
        product.getSpecifications().add(specification);
        productRepository.save(product);
        return productSpecificationMapper.toSpecificationResponse(specification);
    }

    @Override
    public void deleteSpecification(Integer productId, Integer specId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductSpecification spec = productSpecificationRepository.findById(specId)
                .orElseThrow(() -> new AppException(ErrorCode.SPEC_NOT_FOUND));

        if (!product.getSpecifications().contains(spec)) {
            throw new AppException(ErrorCode.SPEC_NOT_BELONG_TO_PRODUCT);
        }

        product.getSpecifications().remove(spec);
        productSpecificationRepository.delete(spec);
        productRepository.save(product);
    }

    // Variant CRUD
    @Override
    @Transactional
    public ProductVariantResponse addVariantToProduct(Integer productId, ProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductVariant variant = productVariantMapper.toProductVariant(product, request);
        product.getVariants().add(variant);
        createVariantSizes(variant, request.getSizes());
        createVariantImages(variant, request.getImages());
        productRepository.save(product);

        return productVariantMapper.toProductVariantResponse(variant);
    }

    @Override
    @Transactional
    public void deleteVariant(Integer productId, Integer variantId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        if (!product.getVariants().contains(variant)) {
            throw new AppException(ErrorCode.VARIANT_NOT_BELONG_TO_PRODUCT);
        }

        product.getVariants().remove(variant);
        productVariantRepository.delete(variant);
        productRepository.save(product);
    }

    /* -- Private methods-- */
    // Specifications
    private void createSpecifications(Product product, List<ProductSpecificationRequest> requests) {
        List<ProductSpecification> specifications = requests.stream()
                .map(s -> productSpecificationMapper.toProductSpecification(product, s))
                .toList();
        product.getSpecifications().addAll(specifications);
        // productSpecificationRepository.saveAll(specifications);
    }

    private void updateSpecification(Product product, List<ProductSpecificationRequest> requests) {
        productSpecificationRepository.deleteAll(product.getSpecifications());
        product.getSpecifications().clear();

        List<ProductSpecification> specifications = requests.stream()
                .map(s -> productSpecificationMapper.toProductSpecification(product, s))
                .toList();
        product.getSpecifications().addAll(specifications);
    }

    // Product variants
    private void createVariant(Product product, ProductVariantRequest request) {
        ProductVariant variant = productVariantMapper.toProductVariant(product, request);
        // variant = productVariantRepository.save(variant);

        product.getVariants().add(variant);
        createVariantSizes(variant, request.getSizes());
        createVariantImages(variant, request.getImages());
    }

    private void updateVariant(Product product, List<ProductVariantRequest> requests) {
        productVariantRepository.deleteAll(product.getVariants());
        product.getVariants().clear();
        requests.forEach(vr -> createVariant(product, vr));
    }

    // Variant sizes
    private void createVariantSizes(ProductVariant variant, List<VariantSizeRequest> requests) {
        List<VariantSize> sizes = requests.stream()
                .map(s -> variantSizeMapper.toVariantSize(variant, s))
                .toList();
        variant.getSizes().addAll(sizes);
        // variantSizeRepository.saveAll(sizes);
    }

    // Variant images
    private void createVariantImages(ProductVariant variant, List<VariantImageRequest> requests) {
        List<VariantImage> images = requests.stream()
                .map(i -> variantImageMapper.toVariantImage(variant, i))
                .toList();
        variant.getImages().addAll(images);
        // variantImageRepository.saveAll(images);
    }
}
