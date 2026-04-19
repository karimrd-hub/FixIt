package com.example.fixit.module.cart.mapper;

import com.example.fixit.module.cart.dto.CartItemResponseDTO;
import com.example.fixit.module.cart.dto.CartResponseDTO;
import com.example.fixit.module.cart.entity.Cart;
import com.example.fixit.module.cart.entity.CartItem;
import com.example.fixit.module.product.entity.Product;
import com.example.fixit.module.product.image.entity.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.brand", target = "brand")
    @Mapping(target = "primaryImageUrl", expression = "java(resolvePrimaryImageUrl(item.getProduct()))")
    @Mapping(target = "lineTotal", expression = "java(computeLineTotal(item))")
    CartItemResponseDTO toItemDTO(CartItem item);

    @Mapping(target = "totalAmount", expression = "java(computeTotal(cart))")
    CartResponseDTO toDTO(Cart cart);

    default String resolvePrimaryImageUrl(Product product) {
        ProductImage primary = product.getPrimaryImage();
        return primary != null ? primary.getUrl() : null;
    }

    default BigDecimal computeLineTotal(CartItem item) {
        if (item.getUnitPrice() == null || item.getQuantity() == null) {
            return BigDecimal.ZERO;
        }
        return item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    default BigDecimal computeTotal(Cart cart) {
        if (cart.getItems() == null) {
            return BigDecimal.ZERO;
        }
        return cart.getItems().stream()
                .map(this::computeLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
