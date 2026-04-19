package com.example.fixit.module.cart.dto;

import com.example.fixit.module.cart.entity.CartStatus;

import java.math.BigDecimal;
import java.util.List;

public record CartResponseDTO(
        Long id,
        CartStatus status,
        List<CartItemResponseDTO> items,
        BigDecimal totalAmount
) {}
