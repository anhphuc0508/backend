// File: com/project/btl/service/OrderService.java
package com.project.btl.service;
import com.project.btl.dto.request.CreateOrderRequest;
import com.project.btl.dto.response.OrderDetailResponse;
import com.project.btl.dto.response.OrderResponse;
import com.project.btl.model.entity.OrderDetail;
import com.project.btl.model.entity.Product;
import com.project.btl.model.entity.ProductVariant;

import java.util.List;
public interface OrderService {
    /**
     * Tạo một đơn hàng mới.
     * Tạm thời chúng ta chưa xử lý user ID (sẽ thêm sau khi có Auth)
     */
    OrderResponse createOrder(CreateOrderRequest request);
    /**
     * Lấy chi tiết đơn hàng theo ID.
     */
    OrderResponse getOrderById(Integer orderId);
/**
 * Lấy lịch sử đơn hàng của một User (sẽ dùng sau khi có Auth)
 */
// List<OrderResponse> getOrdersByUserId(Integer userId);
    /**
     * Hủy một đơn hàng
     */
    OrderResponse cancelOrder(Integer orderId);
    List<OrderResponse> getAllOrders();
    List<OrderResponse> getOrdersByUserId(Integer userId);
    private OrderDetailResponse mapEntityToOrderDetailResponse(OrderDetail detail) {
        ProductVariant variant = detail.getVariant();

        // Cần lấy Product từ Variant để có tên sản phẩm
        // (Giả định ProductVariant của bạn có link đến Product)
        Product product = (variant != null) ? variant.getProduct() : null;

        return OrderDetailResponse.builder()
                .variantId((variant != null) ? variant.getVariantId() : null) // (variantId là giả định)
                .productName((product != null) ? product.getName() : "Sản phẩm không rõ")
                .variantName((variant != null) ? variant.getName() : "Biến thể không rõ")
                .quantity(detail.getQuantity())
                .priceAtPurchase(detail.getPriceAtPurchase())
                .build();
    }
}