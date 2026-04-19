package com.example.fixit.module.cart.dto;

import java.math.BigDecimal;

public record CartItemResponseDTO(
        Long id,
        Long productId,
        String productName,
        String brand,
        String primaryImageUrl,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {}
