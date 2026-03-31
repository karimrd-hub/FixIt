package com.example.fixit.module.catalog.service;

import com.example.fixit.common.exception.BadRequestException;
import com.example.fixit.module.catalog.category.ProductCategory;
import com.example.fixit.module.catalog.dto.CatalogResponseDTO;
import com.example.fixit.module.catalog.dto.ProductFilterDTO;
import com.example.fixit.module.catalog.mapper.CatalogMapper;
import com.example.fixit.module.product.entity.Product;
import com.example.fixit.module.product.repository.ProductRepository;
import com.example.fixit.module.product.specification.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogServiceImpl implements CatalogService {

    private final ProductRepository productRepository;
    private final CatalogMapper catalogMapper;

    /*
     * Fetches active products by category, prioritizing stores in the user's city.
     * Falls back to stores in other cities if no local results are found.
     */
    @Override
    public Page<CatalogResponseDTO> getProductsByCategory(String categorySlug, String city, Pageable pageable) {
        log.info("Looking up products for category='{}', city='{}'", categorySlug, city);

        if(city == null || city.isBlank()){
            log.warn("Rejected request: city parameter is blank");
            throw new BadRequestException("City must not be blank");
        }

        Class<? extends Product> type = ProductCategory.fromSlug(categorySlug).getEntityType();
        log.debug("Resolved category slug '{}' to entity type {}", categorySlug, type.getSimpleName());

        Specification<Product> localSpec = Specification.where(ProductSpecifications.storeIsActive())
                .and(ProductSpecifications.byCategory(type))
                .and(ProductSpecifications.byCity(city));

        Page<Product> localProducts = productRepository.findAll(localSpec, pageable);

        log.debug("Found {} local products in city='{}'", localProducts.getTotalElements(), city);

        if(localProducts.hasContent()){
            return localProducts.map(p -> catalogMapper.toDTO(p, "local"));
        }

        log.info("No local products found, falling back to remote stores for category='{}'", categorySlug);


        Specification<Product> remoteSpec = Specification.where(ProductSpecifications.storeIsActive())
                        .and(ProductSpecifications.byCategory(type)
                        .and(ProductSpecifications.byCityExcluding(city)));


        Page<Product> remoteProducts = productRepository.findAll(remoteSpec, pageable);
        log.debug("Found {} remote products outside city='{}'", remoteProducts.getTotalElements(), city);

        return remoteProducts.map(p -> catalogMapper.toDTO(p, "remote"));
    }


    /*
     * General-purpose filter — any combination of filters from ProductFilterDTO can be applied.
     * All fields are optional; only non-null values are included in the query.
     */
    @Override
    public Page<CatalogResponseDTO> filterProducts(ProductFilterDTO filter, Pageable pageable) {
        log.info("Filtering products with filter={}", filter);

        Specification<Product> spec = ProductSpecifications.fromFilter(filter);
        Page<Product> results = productRepository.findAll(spec, pageable);

        log.debug("Filter returned {} products", results.getTotalElements());
        return results.map(p -> catalogMapper.toDTO(p, null));
    }
}
