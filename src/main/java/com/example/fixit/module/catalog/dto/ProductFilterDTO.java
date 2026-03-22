package com.example.fixit.module.catalog.dto;

import com.example.fixit.module.catalog.category.ProductCategory;
import com.example.fixit.module.product.entity.Product;
import com.example.fixit.module.product.entity.ProductCondition;
import com.example.fixit.module.product.entity.ProductStatus;
import com.example.fixit.module.store.entity.FullfillmentMode;
import com.example.fixit.module.store.entity.StoreServiceType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter

// This DTO class carries all the optional params cleanly from the controller to the service layer, rather than passing 7+ individual parameters to the productRepository methods.
public class ProductFilterDTO {
    private String categorySlug;       // optional — resolves to ProductCategory → entity type
    private String governorate;        // optional
    private String district;           // optional
    private String city;               // optional
    private ProductCondition condition; // optional — NEW, USED
    private ProductStatus status;      // optional — ACTIVE, INACTIVE, OUT_OF_STOCK, DISCONTINUED
    private FullfillmentMode fulfillmentMode;  // optional — IN_SHOP, DELIVERY, HYBRID
    private StoreServiceType serviceType;      // optional — AUTOPARTS_ONLY, AUTOPARTS_AND_REPAIR
    private String brand;              // optional — exact match on product brand
    private BigDecimal priceMin;       // optional — inclusive lower bound
    private BigDecimal priceMax;       // optional — inclusive upper bound
    private String searchTerm;         // optional — LIKE match across name, brand, description

    /**
     * Resolves categorySlug to the corresponding Product subclass type.
     * Returns null if categorySlug is not set (allows filtering without category restriction).
     */
    public Class<? extends Product> getCategoryType() {
        if (categorySlug == null || categorySlug.isBlank()) {
            return null;
        }
        return ProductCategory.fromSlug(categorySlug).getEntityType();
    }
}
