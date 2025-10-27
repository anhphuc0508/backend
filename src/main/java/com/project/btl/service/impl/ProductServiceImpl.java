// File: com/project/btl/service/impl/ProductServiceImpl.java
package com.project.btl.service.impl;

import com.project.btl.dto.response.ProductResponse;
import com.project.btl.dto.response.ProductVariantResponse;
import com.project.btl.exception.ResourceNotFoundException;
import com.project.btl.model.entity.Product;
import com.project.btl.repository.ProductRepository;
import com.project.btl.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Tự động @Autowired qua constructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<ProductResponse> getAllProducts() {
        // Lấy tất cả Product từ DB
        List<Product> products = productRepository.findAll();

        // Chuyển đổi List<Product> sang List<ProductResponse>
        return products.stream()
                .map(this::convertToProductResponse) // Gọi hàm chuyển đổi
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductById(Integer productId) {
        // Tìm product theo ID, nếu không thấy thì "ném" ra lỗi 404
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));

        // Chuyển đổi Entity sang DTO và trả về
        return convertToProductResponse(product);
    }

    // --- Phương thức private để chuyển đổi Entity -> DTO ---

    private ProductResponse convertToProductResponse(Product product) {
        // Chuyển đổi danh sách ProductVariant (Entity) sang ProductVariantResponse (DTO)
        List<ProductVariantResponse> variantResponses = product.getVariants().stream()
                .map(variant -> ProductVariantResponse.builder()
                        .variantId(variant.getVariantId())
                        .name(variant.getName())
                        .sku(variant.getSku())
                        .price(variant.getPrice())
                        .salePrice(variant.getSalePrice())
                        .stockQuantity(variant.getStockQuantity())
                        .build())
                .collect(Collectors.toList());

        // Xây dựng ProductResponse
        return ProductResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                // Lấy tên, tránh lỗi N+1 nếu lazy loading (cần tối ưu sau)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .variants(variantResponses)
                .build();
    }
}