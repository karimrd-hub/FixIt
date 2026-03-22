package com.example.fixit.module.catalog.dto;

import java.math.BigDecimal;

public record CatalogResponseDTO(
        Long productId,
        String name,
        String brand,
        BigDecimal price,
        Long stock,
        String primaryImageUrl,
        String storeName,
        String storeCity,
        BigDecimal storeRating,
        String source
) {}
