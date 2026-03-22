package com.example.fixit.module.catalog.service;

import com.example.fixit.module.catalog.dto.CatalogResponseDTO;
import com.example.fixit.module.catalog.dto.ProductFilterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CatalogService {
    Page<CatalogResponseDTO> getProductsByCategory(String categorySlug, String city, Pageable pageable);
    
    Page<CatalogResponseDTO> filterProducts(ProductFilterDTO filter, Pageable pageable);
}
