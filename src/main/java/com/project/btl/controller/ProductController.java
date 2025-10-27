// File: com/project/btl/controller/ProductController.java
package com.project.btl.controller;

import com.project.btl.dto.response.ProductResponse;
import com.project.btl.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products") // Tiền tố chung cho API sản phẩm
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * API để lấy tất cả sản phẩm
     * Frontend gọi: GET http://localhost:8080/api/v1/products
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products); // Trả về 200 OK
    }

    /**
     * API để lấy chi tiết 1 sản phẩm theo ID
     * Frontend gọi: GET http://localhost:8080/api/v1/products/1 (ví dụ)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Integer id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product); // Trả về 200 OK
    }
}