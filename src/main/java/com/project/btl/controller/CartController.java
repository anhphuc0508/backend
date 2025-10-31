// File: com/project/btl/controller/CartController.java
package com.project.btl.controller;

import com.project.btl.dto.request.CartItemRequest;
import com.project.btl.dto.response.CartResponse;
import com.project.btl.model.entity.User;
import com.project.btl.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart") // [cite: 235]
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add") // [cite: 239]
    public ResponseEntity<CartResponse> addItemToCart(@AuthenticationPrincipal User user , @RequestBody CartItemRequest request){
        Integer userId = user.getUserId();
        CartResponse updatedCart = cartService.additemtoCart(userId, request);
        return ResponseEntity.ok(updatedCart);
    }

    @GetMapping // [cite: 246]
    public ResponseEntity<CartResponse> getMyCart(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        Integer userId = user.getUserId();
        CartResponse cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    // --- BỔ SUNG 2 ENDPOINT NÀY ---

    /**
     * API để cập nhật số lượng item
     * Frontend gọi: PUT http://localhost:8080/api/v1/cart/update
     * Body: { "variantID": "SKU_123", "quantity": 3 }
     */
    @PutMapping("/update")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @AuthenticationPrincipal User user,
            @RequestBody CartItemRequest request) { // Dùng lại CartItemRequest

        Integer userId = user.getUserId();
        CartResponse updatedCart = cartService.updateItemQuantity(userId, request);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * API để xóa item
     * Frontend gọi: DELETE http://localhost:8080/api/v1/cart/remove/SKU_123
     */
    @DeleteMapping("/remove/{sku}")
    public ResponseEntity<CartResponse> removeItemFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable String sku) { // Lấy SKU từ đường dẫn

        Integer userId = user.getUserId();
        CartResponse updatedCart = cartService.removeItemFromCart(userId, sku);
        return ResponseEntity.ok(updatedCart);
    }

    // (Bạn cũng nên thêm 1 API để xóa sạch giỏ hàng)
    // @DeleteMapping("/clear")
    // ...
}