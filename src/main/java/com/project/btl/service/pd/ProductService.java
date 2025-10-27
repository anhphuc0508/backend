// File: com/project/btl/service/ProductService.java
package com.project.btl.service;

import com.project.btl.dto.response.ProductResponse;
import java.util.List;

public interface ProductService {
    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(Integer productId);
    // ... (Thêm các hàm cho create, update, delete sau)
}