package com.example.fixit.module.product.specification;

import com.example.fixit.module.catalog.dto.ProductFilterDTO;
import com.example.fixit.module.product.entity.Product;
import com.example.fixit.module.product.entity.ProductCondition;
import com.example.fixit.module.product.entity.ProductStatus;
import com.example.fixit.module.store.entity.FullfillmentMode;
import com.example.fixit.module.store.entity.Store;
import com.example.fixit.module.store.entity.StoreServiceType;
import com.example.fixit.module.store.entity.StoreStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecifications {

    private ProductSpecifications() {}

    /** Filters by product subtype using the JOINED inheritance discriminator. */
    public static Specification<Product> byCategory(Class<? extends Product> type) {
        return (root, query, cb) -> cb.equal(root.type(), type);
    }

    /** Only products whose owning store is ACTIVE. */
    public static Specification<Product> storeIsActive() {
        return (root, query, cb) -> {
            Join<Product, Store> store = root.join("store", JoinType.INNER);
            return cb.equal(store.get("status"), StoreStatus.ACTIVE);
        };
    }

    public static Specification<Product> byStatus(ProductStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Product> byCondition(ProductCondition condition) {
        if (condition == null) return null;
        return (root, query, cb) -> cb.equal(root.get("condition"), condition);
    }

    public static Specification<Product> byGovernorate(String governorate) {
        if (governorate == null || governorate.isBlank()) return null;
        return (root, query, cb) -> {
            Join<Product, Store> store = joinStore(root);
            return cb.equal(cb.lower(store.get("governorate")), governorate.toLowerCase());
        };
    }

    public static Specification<Product> byDistrict(String district) {
        if (district == null || district.isBlank()) return null;
        return (root, query, cb) -> {
            Join<Product, Store> store = joinStore(root);
            return cb.equal(cb.lower(store.get("district")), district.toLowerCase());
        };
    }

    public static Specification<Product> byCity(String city) {
        if (city == null || city.isBlank()) return null;
        return (root, query, cb) -> {
            Join<Product, Store> store = joinStore(root);
            return cb.equal(cb.lower(store.get("city")), city.toLowerCase());
        };
    }

    public static Specification<Product> byCityExcluding(String city) {
        if (city == null || city.isBlank()) return null;
        return (root, query, cb) -> {
            Join<Product, Store> store = joinStore(root);
            return cb.notEqual(cb.lower(store.get("city")), city.toLowerCase());
        };
    }

    public static Specification<Product> byFulfillmentMode(FullfillmentMode mode) {
        if (mode == null) return null;
        return (root, query, cb) -> {
            Join<Product, Store> store = joinStore(root);
            return cb.equal(store.get("fulfillmentMode"), mode);
        };
    }

    public static Specification<Product> byServiceType(StoreServiceType serviceType) {
        if (serviceType == null) return null;
        return (root, query, cb) -> {
            Join<Product, Store> store = joinStore(root);
            return cb.equal(store.get("serviceType"), serviceType);
        };
    }

    /** Exact case-insensitive match on product brand. */
    public static Specification<Product> byBrand(String brand) {
        if (brand == null || brand.isBlank()) return null;
        return (root, query, cb) -> cb.equal(cb.lower(root.get("brand")), brand.toLowerCase());
    }

    /** Inclusive price range — either bound can be null to apply only one side. */
    public static Specification<Product> byPriceRange(BigDecimal min, BigDecimal max) {
        if (min == null && max == null) return null;
        return (root, query, cb) -> {
            if (min != null && max != null) return cb.between(root.get("price"), min, max);
            if (min != null)               return cb.greaterThanOrEqualTo(root.get("price"), min);
            return                                cb.lessThanOrEqualTo(root.get("price"), max);
        };
    }

    /** Case-insensitive LIKE search across name, brand, and description. */
    public static Specification<Product> bySearchTerm(String term) {
        if (term == null || term.isBlank()) return null;
        return (root, query, cb) -> {
            String pattern = "%" + term.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name")),        pattern),
                cb.like(cb.lower(root.get("brand")),       pattern),
                cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    /**
     * Builds a combined Specification from a ProductFilterDTO.
     * Each null field is simply skipped — only non-null filters are applied.
     */
    public static Specification<Product> fromFilter(ProductFilterDTO filter) {
        Specification<Product> spec = storeIsActive();

        if (filter.getCategoryType() != null) {
            spec = spec.and(byCategory(filter.getCategoryType()));
        }
        spec = spec.and(byStatus(filter.getStatus()));
        spec = spec.and(byCondition(filter.getCondition()));
        spec = spec.and(byGovernorate(filter.getGovernorate()));
        spec = spec.and(byDistrict(filter.getDistrict()));
        spec = spec.and(byCity(filter.getCity()));
        spec = spec.and(byFulfillmentMode(filter.getFulfillmentMode()));
        spec = spec.and(byServiceType(filter.getServiceType()));
        spec = spec.and(byBrand(filter.getBrand()));
        spec = spec.and(byPriceRange(filter.getPriceMin(), filter.getPriceMax()));
        spec = spec.and(bySearchTerm(filter.getSearchTerm()));

        return spec;
    }

    // Reuses an existing store join if present, avoids duplicate joins in the same query.
    @SuppressWarnings("unchecked")
    private static Join<Product, Store> joinStore(jakarta.persistence.criteria.Root<Product> root) {
        return (Join<Product, Store>) root.getJoins().stream()
                .filter(j -> j.getAttribute().getName().equals("store"))
                .findFirst()
                .orElseGet(() -> root.join("store", JoinType.INNER));
    }
}
