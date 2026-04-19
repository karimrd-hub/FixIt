package com.example.fixit.module.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequestDTO(
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity
) {}
