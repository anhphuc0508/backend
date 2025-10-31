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
// ĐÃ SỬA: Thay /api/cart thành /api/v1/cart để khớp với SecurityConfig
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addItemToCart(@AuthenticationPrincipal User user , @RequestBody CartItemRequest request){
        Integer userId = user.getUserId();
        CartResponse updatedCart = cartService.additemtoCart(userId, request);
        return ResponseEntity.ok(updatedCart);
    }
    @GetMapping
    public ResponseEntity<CartResponse> getMyCart(@AuthenticationPrincipal User user) {
        if (user == null) {
            // Trường hợp này không nên xảy ra vì đã có .authenticated()
            return ResponseEntity.status(401).build();
        }

        Integer userId = user.getUserId(); // [cite: 242]
        CartResponse cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }
}