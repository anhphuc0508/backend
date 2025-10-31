// File: com/project/btl/service/impl/CartService.java
package com.project.btl.service.impl;

// ... (Các import khác giữ nguyên)
// BỔ SUNG IMPORT:
import com.project.btl.dto.request.CartItemRequest;
import com.project.btl.dto.response.CartResponse;
import com.project.btl.exception.ResourceNotFoundException;
import com.project.btl.dto.response.CartItemResponse;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.project.btl.model.entity.*;
import com.project.btl.repository.CartItemRepository;
import com.project.btl.repository.CartRepository;
import com.project.btl.repository.ProductVariantRepository;
import com.project.btl.repository.UserRepository;
import com.project.btl.service.CartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CartResponse additemtoCart(Integer userId, CartItemRequest request) {
        Cart cart = findOrCreateCart(userId);
        ProductVariant variant = variantRepository.findBySku(request.getVariantID())
                // Sửa: Dùng Exception tùy chỉnh
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant (SKU) not found: " + request.getVariantID()));

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartAndVariant(cart, variant);
        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setVariant(variant);
            newItem.setQuantity(request.getQuantity());
            // (Nên thêm giá ở đây để lưu lại giá lúc mua)
            // newItem.setPrice(variant.getPrice());
            cartItemRepository.save(newItem);
        }
        return buildCartResponse(cart);
    }

    @Override
    public CartResponse getCart(Integer userId) {
        Cart cart = findOrCreateCart(userId);
        return buildCartResponse(cart);
    }

    // --- BỔ SUNG CÁC HÀM MỚI ---

    @Override
    @Transactional
    public CartResponse updateItemQuantity(Integer userId, CartItemRequest request) {
        // Nếu số lượng <= 0, coi như là xóa
        if (request.getQuantity() <= 0) {
            return removeItemFromCart(userId, request.getVariantID());
        }

        Cart cart = findCartByUserId(userId); // Dùng hàm helper mới
        ProductVariant variant = variantRepository.findBySku(request.getVariantID())
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant (SKU) not found: " + request.getVariantID()));

        // Tìm item
        CartItem item = cartItemRepository.findByCartAndVariant(cart, variant)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        // (Nên kiểm tra tồn kho ở đây)

        // Cập nhật số lượng
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        return buildCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItemFromCart(Integer userId, String sku) {
        Cart cart = findCartByUserId(userId);

        ProductVariant variant = variantRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product Variant (SKU) not found: " + sku));

        // Dùng phương thức mới trong Repository
        cartItemRepository.deleteByCartAndVariant(cart, variant);

        return buildCartResponse(cart);
    }


    // --- CÁC HÀM HELPER (Private) ---

    private Cart findOrCreateCart(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    // BỔ SUNG HÀM NÀY: Chỉ tìm, không tạo mới
    private Cart findCartByUserId(Integer userId) {
        // Dùng phương thức findByUser_UserId mà bạn đã cung cấp [cite: 1452]
        return cartRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));
    }

    // THAY THẾ HÀM NÀY (để trả về 'items' cho frontend)
    private CartResponse buildCartResponse(Cart cart) {
        Cart updatedCart = cartRepository.findById(cart.getCartId())
                .orElse(cart);

        // Dùng findByCart (từ CartItemRepository)
        List<CartItem> cartItems = cartItemRepository.findByCart(updatedCart);

        // --- LOGIC MAP (CHUYỂN ĐỔI) ---
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(item -> {
                    ProductVariant variant = item.getVariant();
                    Product product = variant.getProduct();

                    // (Code lấy ảnh, bạn có thể sửa sau)
                    String imageUrl = (product.getImages() != null && !product.getImages().isEmpty())
                            ? product.getImages().stream().findFirst().map(ProductImage::getImageUrl).orElse(null)
                            : null;

                    return CartItemResponse.builder()
                            .variantId(variant.getVariantId())
                            .sku(variant.getSku())
                            .name(product.getName() + " - " + variant.getName()) // Ghép tên
                            // (Cần sửa logic giá: Lấy giá từ CartItem thay vì Variant)
                            .price(variant.getPrice())
                            .quantity(item.getQuantity())
                            .image(imageUrl)
                            .build();
                })
                .collect(Collectors.toList());

        int total = itemResponses.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        // Trả về response đầy đủ
        return CartResponse.builder()
                .totalItems(total)
                .items(itemResponses)
                .build();
    }
}