// File: com/project/btl/repository/CartItemRepository.java
package com.project.btl.repository;
import com.project.btl.model.entity.Cart;
import com.project.btl.model.entity.CartItem;
import com.project.btl.model.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByCartAndVariant(Cart cart, ProductVariant variant);
}