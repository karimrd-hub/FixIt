package com.example.fixit.module.catalog.controller;

import com.example.fixit.module.catalog.category.ProductCategory;
import com.example.fixit.module.catalog.dto.CatalogResponseDTO;
import com.example.fixit.module.catalog.dto.ProductFilterDTO;
import com.example.fixit.module.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, String>>> getCategories() {
        log.info("Fetching all product categories");
        List<Map<String, String>> categories = Arrays.stream(ProductCategory.values())
                .map(c -> Map.of("id", c.getSlug(), "displayName", c.name()))
                .toList();
        log.debug("Returning {} categories", categories.size());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/{categorySlug}/stores")
    public ResponseEntity<Page<CatalogResponseDTO>> getStoresByProductCategory(
            @PathVariable String categorySlug,
            @RequestParam String city,
            @PageableDefault(size = 20, sort = "store.rating", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("Fetching products for category='{}', city='{}'", categorySlug, city);
        Page<CatalogResponseDTO> results = catalogService.getProductsByCategory(categorySlug, city, pageable);
        log.debug("Returning page {}/{} for category='{}', city='{}'", results.getNumber(), results.getTotalPages(), categorySlug, city);
        return ResponseEntity.ok(results);
    }

    static void main() {

    }

    // All params are optional — any combination can be used.
    // Example: GET /api/catalog/products/filter?city=Cairo&status=ACTIVE&serviceType=AUTOPARTS_ONLY
    @GetMapping("/products/filter")
    public ResponseEntity<Page<CatalogResponseDTO>> filterProducts(
            @ModelAttribute ProductFilterDTO filter,
            @PageableDefault(size = 20, sort = "store.rating", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("Filtering products with filter={}", filter);
        Page<CatalogResponseDTO> results = catalogService.filterProducts(filter, pageable);
        log.debug("Filter returned page {}/{}", results.getNumber(), results.getTotalPages());
        return ResponseEntity.ok(results);
    }
}
