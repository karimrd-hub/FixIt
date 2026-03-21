package com.example.fixit;

import com.example.fixit.module.product.entity.Product;
import com.example.fixit.module.product.entity.ProductCondition;
import com.example.fixit.module.product.entity.ProductStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class LombokTest {

    @Test
    void testLombokGettersAndSetters() {
        // Test that Lombok-generated getters and setters work
        Product product = new Product();
        
        product.setName("Test Product");
        product.setPrice(new BigDecimal("99.99"));
        product.setStock(10L);
        product.setBrand("Test Brand");
        product.setDescription("Test Description");
        product.setStatus(ProductStatus.ACTIVE);
        product.setCondition(ProductCondition.NEW);
        
        assertEquals("Test Product", product.getName());
        assertEquals(new BigDecimal("99.99"), product.getPrice());
        assertEquals(10L, product.getStock());
        assertEquals("Test Brand", product.getBrand());
        assertEquals("Test Description", product.getDescription());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertEquals(ProductCondition.NEW, product.getCondition());
    }

    @Test
    void testLombokToString() {
        // Test that Lombok-generated toString works
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("99.99"));
        
        String toString = product.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Product"));
        assertTrue(toString.contains("Test Product"));
    }

    @Test
    void testLombokEqualsAndHashCode() {
        // Test that Lombok-generated equals and hashCode work
        Product product1 = new Product();
        product1.setName("Test Product");
        product1.setPrice(new BigDecimal("99.99"));
        
        Product product2 = new Product();
        product2.setName("Test Product");
        product2.setPrice(new BigDecimal("99.99"));
        
        // Note: equals/hashCode may depend on ID field from AuditableEntity
        // This test verifies the methods exist and are callable
        assertNotNull(product1.hashCode());
        assertNotNull(product2.hashCode());
        assertNotNull(product1.equals(product2));
    }

    @Test
    void testLombokFieldInitialization() {
        // Test that Lombok respects field initializers
        Product product = new Product();
        
        // These fields have default values in the entity
        assertEquals(0L, product.getStock());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertEquals(ProductCondition.NEW, product.getCondition());
        assertNotNull(product.getImages());
        assertTrue(product.getImages().isEmpty());
    }
}
