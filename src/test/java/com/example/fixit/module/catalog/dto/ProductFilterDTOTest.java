package com.example.fixit.module.catalog.dto;

import com.example.fixit.module.product.autoparts.brake.entity.Brake;
import com.example.fixit.module.product.autoparts.tire.entity.Tire;
import com.example.fixit.module.product.entity.Product;
import org.junit.jupiter.api.Test;

import com.example.fixit.common.exception.BadRequestException;

import static org.junit.jupiter.api.Assertions.*;

class ProductFilterDTOTest {

    @Test
    void getCategoryType_nullSlug_returnsNull() {
        ProductFilterDTO filter = new ProductFilterDTO();
        filter.setCategorySlug(null);

        assertNull(filter.getCategoryType());
    }

    @Test
    void getCategoryType_blankSlug_returnsNull() {
        ProductFilterDTO filter = new ProductFilterDTO();
        filter.setCategorySlug("   ");

        assertNull(filter.getCategoryType());
    }

    @Test
    void getCategoryType_validBrakeSlug_returnsBrakeClass() {
        ProductFilterDTO filter = new ProductFilterDTO();
        filter.setCategorySlug("brake");

        Class<? extends Product> type = filter.getCategoryType();

        assertEquals(Brake.class, type);
    }

    @Test
    void getCategoryType_validTireSlug_returnsTireClass() {
        ProductFilterDTO filter = new ProductFilterDTO();
        filter.setCategorySlug("tire");

        Class<? extends Product> type = filter.getCategoryType();

        assertEquals(Tire.class, type);
    }

    @Test
    void getCategoryType_invalidSlug_throwsBadRequest() {
        ProductFilterDTO filter = new ProductFilterDTO();
        filter.setCategorySlug("nonexistent");

        assertThrows(BadRequestException.class, filter::getCategoryType);
    }
}
