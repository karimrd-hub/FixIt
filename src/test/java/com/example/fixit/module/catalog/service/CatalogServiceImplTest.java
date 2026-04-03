package com.example.fixit.module.catalog.service;

import com.example.fixit.module.catalog.dto.CatalogResponseDTO;
import com.example.fixit.module.catalog.dto.ProductFilterDTO;
import com.example.fixit.module.catalog.mapper.CatalogMapper;
import com.example.fixit.module.product.autoparts.brake.entity.Brake;
import com.example.fixit.module.product.entity.Product;
import com.example.fixit.module.product.repository.ProductRepository;
import com.example.fixit.module.store.entity.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import com.example.fixit.common.exception.BadRequestException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CatalogMapper catalogMapper;

    @InjectMocks
    private CatalogServiceImpl catalogService;

    private Pageable pageable;
    private Brake sampleBrake;
    private CatalogResponseDTO sampleDTO;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 20);

        Store store = new Store();
        store.setId(1L);
        store.setName("Test Store");
        store.setCity("Achrafieh");
        store.setRating(BigDecimal.valueOf(4.5));

        sampleBrake = new Brake();
        sampleBrake.setId(1L);
        sampleBrake.setName("Brembo Brake");
        sampleBrake.setBrand("Brembo");
        sampleBrake.setPrice(BigDecimal.valueOf(85));
        sampleBrake.setStock(10L);
        sampleBrake.setStore(store);

        sampleDTO = new CatalogResponseDTO(
                1L, "Brembo Brake", "Brembo", BigDecimal.valueOf(85), 10L,
                null, "Test Store", "Achrafieh", BigDecimal.valueOf(4.5), "local"
        );
    }

    // ── getProductsByCategory ─────────────────────────────────────────────────

    @Nested
    class GetProductsByCategoryTests {

        @Test
        void blankCity_throwsBadRequest() {
            assertThrows(BadRequestException.class,
                    () -> catalogService.getProductsByCategory("brake", "", pageable));
        }

        @Test
        void nullCity_throwsBadRequest() {
            assertThrows(BadRequestException.class,
                    () -> catalogService.getProductsByCategory("brake", null, pageable));
        }

        @Test
        void invalidCategorySlug_throwsBadRequest() {
            assertThrows(BadRequestException.class,
                    () -> catalogService.getProductsByCategory("unknown-category", "Achrafieh", pageable));
        }

        @Test
        void localProductsFound_returnsWithLocalSource() {
            Page<Product> localPage = new PageImpl<>(List.of(sampleBrake), pageable, 1);
            when(productRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(localPage);
            when(catalogMapper.toDTO(sampleBrake, "local")).thenReturn(sampleDTO);

            Page<CatalogResponseDTO> result = catalogService.getProductsByCategory("brake", "Achrafieh", pageable);

            assertFalse(result.isEmpty());
            assertEquals("local", result.getContent().get(0).source());
            verify(productRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        void noLocalProducts_fallsBackToRemote() {
            Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            Page<Product> remotePage = new PageImpl<>(List.of(sampleBrake), pageable, 1);

            CatalogResponseDTO remoteDTO = new CatalogResponseDTO(
                    1L, "Brembo Brake", "Brembo", BigDecimal.valueOf(85), 10L,
                    null, "Test Store", "Achrafieh", BigDecimal.valueOf(4.5), "remote"
            );

            when(productRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(emptyPage)
                    .thenReturn(remotePage);
            when(catalogMapper.toDTO(sampleBrake, "remote")).thenReturn(remoteDTO);

            Page<CatalogResponseDTO> result = catalogService.getProductsByCategory("brake", "Achrafieh", pageable);

            assertFalse(result.isEmpty());
            assertEquals("remote", result.getContent().get(0).source());
            verify(productRepository, times(2)).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        void noProductsAnywhere_returnsEmptyPage() {
            Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(productRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(emptyPage);

            Page<CatalogResponseDTO> result = catalogService.getProductsByCategory("brake", "Achrafieh", pageable);

            assertTrue(result.isEmpty());
            verify(productRepository, times(2)).findAll(any(Specification.class), eq(pageable));
        }
    }

    // ── filterProducts ────────────────────────────────────────────────────────

    @Nested
    class FilterProductsTests {

        @Test
        void emptyFilter_delegatesToRepository() {
            ProductFilterDTO filter = new ProductFilterDTO();
            Page<Product> page = new PageImpl<>(List.of(sampleBrake), pageable, 1);

            CatalogResponseDTO dto = new CatalogResponseDTO(
                    1L, "Brembo Brake", "Brembo", BigDecimal.valueOf(85), 10L,
                    null, "Test Store", "Achrafieh", BigDecimal.valueOf(4.5), null
            );

            when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(catalogMapper.toDTO(sampleBrake, null)).thenReturn(dto);

            Page<CatalogResponseDTO> result = catalogService.filterProducts(filter, pageable);

            assertEquals(1, result.getTotalElements());
            assertNull(result.getContent().get(0).source());
        }

        @Test
        void emptyResults_returnsEmptyPage() {
            ProductFilterDTO filter = new ProductFilterDTO();
            filter.setCity("NonExistent");

            when(productRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

            Page<CatalogResponseDTO> result = catalogService.filterProducts(filter, pageable);

            assertTrue(result.isEmpty());
        }
    }
}
