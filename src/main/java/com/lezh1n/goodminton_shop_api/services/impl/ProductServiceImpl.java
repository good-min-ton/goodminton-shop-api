package com.lezh1n.goodminton_shop_api.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.lezh1n.goodminton_shop_api.dtos.request.DiscountRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductSpecificationRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ProductVariantRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.ReviewRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.VariantImageRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.VariantSizeRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.DiscountResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductByAttributeResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductSpecificationResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ProductVariantResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.ReviewResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.SpecificVariantResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.VariantSizeResponse;
import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.entities.Color;
import com.lezh1n.goodminton_shop_api.entities.Product;
import com.lezh1n.goodminton_shop_api.entities.ProductDiscount;
import com.lezh1n.goodminton_shop_api.entities.ProductSpecification;
import com.lezh1n.goodminton_shop_api.entities.ProductVariant;
import com.lezh1n.goodminton_shop_api.entities.Review;
import com.lezh1n.goodminton_shop_api.entities.Size;
import com.lezh1n.goodminton_shop_api.entities.VariantImage;
import com.lezh1n.goodminton_shop_api.entities.VariantSize;
import com.lezh1n.goodminton_shop_api.entities.Version;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.ColorMapper;
import com.lezh1n.goodminton_shop_api.mappers.DiscountMapper;
import com.lezh1n.goodminton_shop_api.mappers.ProductMapper;
import com.lezh1n.goodminton_shop_api.mappers.ProductSpecificationMapper;
import com.lezh1n.goodminton_shop_api.mappers.ProductVariantMapper;
import com.lezh1n.goodminton_shop_api.mappers.ReviewMapper;
import com.lezh1n.goodminton_shop_api.mappers.VariantImageMapper;
import com.lezh1n.goodminton_shop_api.mappers.VariantSizeMapper;
import com.lezh1n.goodminton_shop_api.mappers.VersionMapper;
import com.lezh1n.goodminton_shop_api.repositories.ColorRepository;
import com.lezh1n.goodminton_shop_api.repositories.InventoryRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductDiscountRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductSpecificationRepository;
import com.lezh1n.goodminton_shop_api.repositories.ProductVariantRepository;
import com.lezh1n.goodminton_shop_api.repositories.ReviewRepository;
import com.lezh1n.goodminton_shop_api.repositories.SizeRepository;
import com.lezh1n.goodminton_shop_api.repositories.VariantSizeRepository;
import com.lezh1n.goodminton_shop_api.repositories.VersionRepository;
import com.lezh1n.goodminton_shop_api.security.CurrentAccountProvider;
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
    private final VersionRepository versionRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductDiscountRepository productDiscountRepository;
    private final VariantSizeRepository variantSizeRepository;
    private final ReviewRepository reviewRepository;
    private final ProductMapper productMapper;
    private final ProductVariantMapper productVariantMapper;
    private final ProductSpecificationMapper productSpecificationMapper;
    private final VariantSizeMapper variantSizeMapper;
    private final VariantImageMapper variantImageMapper;
    private final VersionMapper versionMapper;
    private final ColorMapper colorMapper;
    private final DiscountMapper discountMapper;
    private final ReviewMapper reviewMapper;
    private final CurrentAccountProvider currentAccountProvider;

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

                    vr.setSizes(variant.getSizes().stream().map(
                            variantSize -> {
                                VariantSizeResponse vs = variantSizeMapper.toVariantSizeResponse(variantSize);

                                BigDecimal discountPrice = null;
                                Optional<ProductDiscount> discount = productDiscountRepository
                                        .findActiveDiscountByVariantSizeId(id,
                                                LocalDateTime.now());
                                if (discount.isPresent()) {
                                    discountPrice = discount.get().getSalePrice();
                                }
                                vs.setDiscountPrice(discountPrice);

                                return vs;
                            }).toList());

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
        ProductSpecification savedSpecification = productSpecificationRepository.save(specification);
        product.getSpecifications().add(savedSpecification);
        productRepository.save(product);
        return productSpecificationMapper.toSpecificationResponse(savedSpecification);
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
        ProductVariant savedVariant = productVariantRepository.save(variant);

        createVariantSizes(savedVariant, request.getSizes());
        createVariantImages(savedVariant, request.getImages());

        productRepository.save(product);
        return productVariantMapper.toProductVariantResponse(savedVariant);
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

    // Get product by attributes
    @Override
    public ProductByAttributeResponse getProductByAttributes(Integer productId, Integer versionId, Integer colorId,
            Integer sizeId) {
        Optional<Version> version = versionRepository.findById(versionId);
        Optional<Color> color = colorRepository.findById(colorId);
        Optional<Size> size = sizeRepository.findById(sizeId);
        if (version.isEmpty() || color.isEmpty() || size.isEmpty()) {
            throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductVariant variant = productVariantRepository.findVariantByAttribute(productId, versionId, colorId)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        VariantSize variantSize = variant.getSizes().stream()
                .filter(s -> s.getSize().getSizeId().equals(sizeId))
                .findFirst().orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        if (!inventoryRepository.existsByVariantSizeVariantSizeId(variantSize.getVariantSizeId())) {
            throw new AppException(ErrorCode.INVENTORY_VARIANT_NOT_FOUND);
        }

        return ProductByAttributeResponse.builder()
                .productId(productId)
                .name(product.getName())
                .description(product.getDescription())
                .thumbnailUrl(product.getThumbnailUrl())
                .createAt(product.getCreateAt())
                .variant(SpecificVariantResponse.builder()
                        .variantId(variant.getVariantId())
                        .version(versionMapper.toVersionResponse(version.get()))
                        .color(colorMapper.toColorResponse(color.get()))
                        .size(variantSizeMapper.toVariantSizeResponse(variantSize))
                        .images(variant.getImages().stream()
                                .map(variantImageMapper::toVariantImageResponse)
                                .toList())
                        .build())
                .build();
    }

    // Product discount
    @Override
    public DiscountResponse createDiscount(Integer variantSizeId, DiscountRequest request) {

        VariantSize variantSize = variantSizeRepository.findById(variantSizeId)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_SIZE_NOT_FOUND));

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.DISCOUNT_START_TIME_BEFORE_NOW);
        }

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new AppException(ErrorCode.DISCOUNT_END_TIME_BEFORE_START_TIME);
        }

        if (productDiscountRepository.existByVariantSizeAndTime(variantSize.getVariantSizeId(), request.getStartTime(),
                request.getEndTime())) {
            throw new AppException(ErrorCode.DISCOUNT_EXISTED);
        }

        ProductDiscount discount = discountMapper.toProductDiscount(variantSize, request);

        return discountMapper.toDiscountResponse(productDiscountRepository.save(discount));
    }

    // Product reviews
    @Override
    public ReviewResponse createReview(Integer productId, ReviewRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Account account = currentAccountProvider.getCurrentAccount();

        Review review = reviewMapper.toReview(product, request);
        review.setUser(account);

        return reviewMapper.toReviewResponse(reviewRepository.save(review));
    }

    @Override
    public Page<ReviewResponse> getReviewsOfProduct(Integer productId, int page, int size, String sortBy,
            String sortDir) {
        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Review> reviewPage = reviewRepository.findByProductProductId(productId, pageable);

        return reviewPage.map(reviewMapper::toReviewResponse);
    }

    /* -- Private methods-- */
    // Specifications
    private void createSpecifications(Product product, List<ProductSpecificationRequest> requests) {
        List<ProductSpecification> specifications = requests.stream()
                .map(s -> productSpecificationMapper.toProductSpecification(product, s))
                .toList();
        product.getSpecifications().addAll(specifications);
    }

    private void updateSpecification(Product product, List<ProductSpecificationRequest> requests) {
        productSpecificationRepository.deleteByProductProductId(product.getProductId());
        product.getSpecifications().clear();
        productSpecificationRepository.flush();

        List<ProductSpecification> specifications = requests.stream()
                .map(s -> productSpecificationMapper.toProductSpecification(product, s))
                .toList();
        product.getSpecifications().addAll(specifications);
    }

    // Product variants
    private void createVariant(Product product, ProductVariantRequest request) {
        ProductVariant variant = productVariantMapper.toProductVariant(product, request);

        product.getVariants().add(variant);
        createVariantSizes(variant, request.getSizes());
        createVariantImages(variant, request.getImages());
    }

    private void updateVariant(Product product, List<ProductVariantRequest> requests) {
        productVariantRepository.deleteByProductProductId(product.getProductId());
        product.getVariants().clear();
        productVariantRepository.flush();
        requests.forEach(vr -> createVariant(product, vr));
    }

    // Variant sizes
    private void createVariantSizes(ProductVariant variant, List<VariantSizeRequest> requests) {
        List<VariantSize> sizes = requests.stream()
                .map(s -> variantSizeMapper.toVariantSize(variant, s))
                .toList();
        variant.getSizes().addAll(sizes);
    }

    // Variant images
    private void createVariantImages(ProductVariant variant, List<VariantImageRequest> requests) {
        List<VariantImage> images = requests.stream()
                .map(i -> variantImageMapper.toVariantImage(variant, i))
                .toList();
        variant.getImages().addAll(images);
    }
}
