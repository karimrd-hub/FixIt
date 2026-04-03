package com.example.fixit.module.catalog.controller;

import com.example.fixit.common.exception.BadRequestException;
import com.example.fixit.module.catalog.category.ProductCategory;
import com.example.fixit.module.catalog.dto.CatalogResponseDTO;
import com.example.fixit.module.catalog.dto.ProductFilterDTO;
import com.example.fixit.module.catalog.service.CatalogService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CatalogController.class)
@WithMockUser
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogService catalogService;

    // ── GET /api/catalog/categories ───────────────────────────────────────────

    @Nested
    class GetCategoriesTests {

        @Test
        void returnsAllCategories() throws Exception {
            mockMvc.perform(get("/api/catalog/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(ProductCategory.values().length)))
                    .andExpect(jsonPath("$[0].id", notNullValue()))
                    .andExpect(jsonPath("$[0].displayName", notNullValue()));
        }

        @Test
        void categoriesContainTireSlug() throws Exception {
            mockMvc.perform(get("/api/catalog/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.id == 'tire')]").exists());
        }
    }

    // ── GET /api/catalog/categories/{slug}/stores ─────────────────────────────

    @Nested
    class GetStoresByProductCategoryTests {

        @Test
        void validRequest_returns200() throws Exception {
            CatalogResponseDTO dto = new CatalogResponseDTO(
                    1L, "Brembo Brake", "Brembo", BigDecimal.valueOf(85), 10L,
                    null, "Test Store", "Achrafieh", BigDecimal.valueOf(4.5), "local"
            );
            Page<CatalogResponseDTO> page = new PageImpl<>(List.of(dto));

            when(catalogService.getProductsByCategory(eq("brake"), eq("Achrafieh"), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/catalog/categories/brake/stores")
                            .param("city", "Achrafieh"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].productId").value(1))
                    .andExpect(jsonPath("$.content[0].source").value("local"));
        }

        @Test
        void missingCityParam_returns400() throws Exception {
            mockMvc.perform(get("/api/catalog/categories/brake/stores"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void invalidCategory_serviceThrows_returns400() throws Exception {
            when(catalogService.getProductsByCategory(eq("invalid"), eq("Achrafieh"), any(Pageable.class)))
                    .thenThrow(new BadRequestException("Unknown category: invalid"));

            mockMvc.perform(get("/api/catalog/categories/invalid/stores")
                            .param("city", "Achrafieh"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Unknown category: invalid"));
        }

        @Test
        void emptyResults_returns200WithEmptyPage() throws Exception {
            Page<CatalogResponseDTO> emptyPage = new PageImpl<>(Collections.emptyList());

            when(catalogService.getProductsByCategory(eq("brake"), eq("Tripoli"), any(Pageable.class)))
                    .thenReturn(emptyPage);

            mockMvc.perform(get("/api/catalog/categories/brake/stores")
                            .param("city", "Tripoli"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }
    }

    // ── GET /api/catalog/products/filter ──────────────────────────────────────

    @Nested
    class FilterProductsTests {

        @Test
        void noParams_returns200() throws Exception {
            Page<CatalogResponseDTO> page = new PageImpl<>(Collections.emptyList());

            when(catalogService.filterProducts(any(ProductFilterDTO.class), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/catalog/products/filter"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        void withAllParams_returns200() throws Exception {
            CatalogResponseDTO dto = new CatalogResponseDTO(
                    1L, "Brembo Brake", "Brembo", BigDecimal.valueOf(85), 10L,
                    null, "Test Store", "Achrafieh", BigDecimal.valueOf(4.5), null
            );
            Page<CatalogResponseDTO> page = new PageImpl<>(List.of(dto));

            when(catalogService.filterProducts(any(ProductFilterDTO.class), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/catalog/products/filter")
                            .param("categorySlug", "brake")
                            .param("city", "Achrafieh")
                            .param("condition", "NEW")
                            .param("status", "ACTIVE")
                            .param("brand", "Brembo")
                            .param("priceMin", "50")
                            .param("priceMax", "200"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].brand").value("Brembo"));
        }

        @Test
        void withPaginationParams_returns200() throws Exception {
            Page<CatalogResponseDTO> page = new PageImpl<>(Collections.emptyList());

            when(catalogService.filterProducts(any(ProductFilterDTO.class), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/catalog/products/filter")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "price,asc"))
                    .andExpect(status().isOk());
        }
    }
}
