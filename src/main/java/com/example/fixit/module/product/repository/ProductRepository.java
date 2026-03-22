package com.example.fixit.module.product.repository;

import com.example.fixit.module.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 
 * Extends JpaSpecificationExecutor to enable dynamic filtering using JPA Specifications.
 * This allows combining multiple optional filters (status, condition, location, etc.)
 * without writing separate query methods for each combination.
 * 
 * All composable query logic lives in {com.example.fixit.module.product.specification.ProductSpecifications}
 */
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
}
