// File: com/project/btl/service/impl/ProductServiceImpl.java
package com.project.btl.service.impl;

import com.project.btl.dto.request.CreateProductRequest;
import com.project.btl.dto.request.ProductVariantRequest;
import com.project.btl.dto.response.ProductResponse;
import com.project.btl.dto.response.ProductVariantResponse;
import com.project.btl.exception.ResourceNotFoundException;
import com.project.btl.model.entity.Brand;
import com.project.btl.model.entity.Category;
import com.project.btl.model.entity.Product;
import com.project.btl.model.entity.ProductVariant;
import com.project.btl.repository.BrandRepository;
import com.project.btl.repository.CategoryRepository;
import com.project.btl.repository.ProductRepository;
import com.project.btl.repository.ProductVariantRepository;
import com.project.btl.service.ProductService;
import com.project.btl.service.ReviewService; // <-- BƯỚC 2: IMPORT
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Sẽ tự động tiêm 5 repository/service bên dưới
public class ProductServiceImpl implements ProductService { // <- "implements" ProductService
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ReviewService reviewService; // <-- BƯỚC 3: TIÊM SERVICE

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToProductResponse) // Hàm này đã được sửa
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm ID: " + productId));
        return convertToProductResponse(product); // Hàm này đã được sửa
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Category ID: " + request.getCategoryId()));
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Brand ID: " + request.getBrandId()));
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setBrand(brand);
        Set<ProductVariant> variants = new HashSet<>();
        for (ProductVariantRequest variantRequest : request.getVariants()) {
            if (productVariantRepository.findBySku(variantRequest.getSku()).isPresent()) {
                throw new IllegalArgumentException("SKU đã tồn tại: " + variantRequest.getSku());
            }
            ProductVariant variant = new ProductVariant();
            variant.setName(variantRequest.getName());
            variant.setSku(variantRequest.getSku());
            variant.setPrice(variantRequest.getPrice());
            variant.setSalePrice(variantRequest.getSalePrice());
            variant.setStockQuantity(variantRequest.getStockQuantity());
            variant.setProduct(product);
            variants.add(variant);
        }
        product.setVariants(variants);
        Product savedProduct = productRepository.save(product);
        return convertToProductResponse(savedProduct); // Hàm này đã được sửa
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Integer productId, CreateProductRequest request) {
        // 1. Tìm các đối tượng liên quan
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Category ID: " + request.getCategoryId()));
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Brand ID: " + request.getBrandId()));
        // 2. Cập nhật thông tin Product
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setBrand(brand);
        // 3. Logic "Diff/Merge" cho Variants
        Map<String, ProductVariantRequest> newVariantsMap = request.getVariants().stream()
                .collect(Collectors.toMap(
                        ProductVariantRequest::getSku,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
        Map<String, ProductVariant> oldVariantsMap = product.getVariants().stream()
                .collect(Collectors.toMap(ProductVariant::getSku, Function.identity()));
        Set<ProductVariant> finalVariants = new HashSet<>();
        for (ProductVariantRequest newVariantReq : request.getVariants()) {
            ProductVariant existingVariant = oldVariantsMap.get(newVariantReq.getSku());
            if (existingVariant != null) {
                existingVariant.setName(newVariantReq.getName());
                existingVariant.setPrice(newVariantReq.getPrice());
                existingVariant.setSalePrice(newVariantReq.getSalePrice());
                existingVariant.setStockQuantity(newVariantReq.getStockQuantity());
                finalVariants.add(existingVariant);
                oldVariantsMap.remove(newVariantReq.getSku());
            } else {
                if (productVariantRepository.findBySku(newVariantReq.getSku()).isPresent()) {
                    throw new IllegalArgumentException("SKU đã tồn tại trên một sản phẩm khác: " + newVariantReq.getSku());
                }
                ProductVariant newVariant = new ProductVariant();
                newVariant.setName(newVariantReq.getName());
                newVariant.setSku(newVariantReq.getSku());
                newVariant.setPrice(newVariantReq.getPrice());
                newVariant.setSalePrice(newVariantReq.getSalePrice());
                newVariant.setStockQuantity(newVariantReq.getStockQuantity());
                newVariant.setProduct(product);
                finalVariants.add(newVariant);
            }
        }
        product.getVariants().clear();
        product.getVariants().addAll(finalVariants);
        Product updatedProduct = productRepository.save(product);
        return convertToProductResponse(updatedProduct); // Hàm này đã được sửa
    }

    @Override
    @Transactional
    public void deleteProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));
        productRepository.delete(product);
    }

    // --- BƯỚC 4: SỬA HÀM HELPER ---
    private ProductResponse convertToProductResponse(Product product) {
        List<ProductVariantResponse> variantResponses = null;
        if (product.getVariants() != null) {
            variantResponses = product.getVariants().stream()
                    .map(variant -> ProductVariantResponse.builder()
                            .variantId(variant.getVariantId())
                            .name(variant.getName())
                            .sku(variant.getSku())
                            .price(variant.getPrice())
                            .salePrice(variant.getSalePrice())
                            .stockQuantity(variant.getStockQuantity())
                            .build())
                    .collect(Collectors.toList());
        }

        // LẤY THÊM THỐNG KÊ REVIEW
        Map<String, Object> stats = reviewService.getReviewStatistics(product.getProductId());
        Double avgRating = (Double) stats.get("averageRating");
        Long totalRev = (Long) stats.get("totalReviews");

        return ProductResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .variants(variantResponses)
                .averageRating(avgRating) // <-- GÁN VÀO DTO
                .totalReviews(totalRev)   // <-- GÁN VÀO DTO
                .build();
    }
}
