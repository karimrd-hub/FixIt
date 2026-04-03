package com.example.fixit.module.catalog;

import com.example.fixit.common.TestDataFactory;
import com.example.fixit.module.product.autoparts.brake.entity.Brake;
import com.example.fixit.module.product.autoparts.tire.entity.Tire;
import com.example.fixit.module.product.entity.ProductCondition;
import com.example.fixit.module.product.repository.ProductRepository;
import com.example.fixit.module.store.entity.Store;
import com.example.fixit.module.store.entity.StoreStatus;
import com.example.fixit.module.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class CatalogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    private Store localStore;
    private Store remoteStore;
    private Store inactiveStore;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        storeRepository.deleteAll();

        localStore = storeRepository.save(
                TestDataFactory.store("Local Store", "Beirut", "Beirut", "Achrafieh"));

        remoteStore = storeRepository.save(
                TestDataFactory.store("Remote Store", "North", "Tripoli", "Tripoli"));

        inactiveStore = TestDataFactory.store("Inactive Store", "Beirut", "Beirut", "Hamra");
        inactiveStore.setStatus(StoreStatus.INACTIVE);
        inactiveStore = storeRepository.save(inactiveStore);
    }

    // ── Categories endpoint ───────────────────────────────────────────────────

    @Nested
    class CategoriesEndpoint {

        @Test
        void returnsAllCategories() throws Exception {
            mockMvc.perform(get("/api/catalog/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[?(@.id == 'brake')]").exists())
                    .andExpect(jsonPath("$[?(@.id == 'tire')]").exists())
                    .andExpect(jsonPath("$[?(@.id == 'battery')]").exists());
        }
    }

    // ── Category browsing (local/remote fallback) ─────────────────────────────

    @Nested
    class CategoryBrowsing {

        @Test
        void localProductsExist_returnsLocalSource() throws Exception {
            productRepository.save(TestDataFactory.brake(localStore));

            mockMvc.perform(get("/api/catalog/categories/brake/stores")
                            .param("city", "Achrafieh"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].source").value("local"))
                    .andExpect(jsonPath("$.content[0].storeName").value("Local Store"));
        }

        @Test
        void noLocalProducts_fallsBackToRemote() throws Exception {
            productRepository.save(TestDataFactory.brake(remoteStore));

            mockMvc.perform(get("/api/catalog/categories/brake/stores")
                            .param("city", "Achrafieh"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].source").value("remote"))
                    .andExpect(jsonPath("$.content[0].storeName").value("Remote Store"));
        }

        @Test
        void inactiveStoreProducts_excluded() throws Exception {
            productRepository.save(TestDataFactory.brake(inactiveStore));

            mockMvc.perform(get("/api/catalog/categories/brake/stores")
                            .param("city", "Hamra"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        void invalidCategory_returns400() throws Exception {
            mockMvc.perform(get("/api/catalog/categories/unknown/stores")
                            .param("city", "Achrafieh"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Unknown category: unknown"));
        }

        @Test
        void missingCity_returns400() throws Exception {
            mockMvc.perform(get("/api/catalog/categories/brake/stores"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── Filter endpoint (end-to-end) ──────────────────────────────────────────

    @Nested
    class FilterEndpoint {

        @Test
        void noFilters_returnsAllActiveStoreProducts() throws Exception {
            productRepository.save(TestDataFactory.brake(localStore));
            productRepository.save(TestDataFactory.tire(remoteStore));
            productRepository.save(TestDataFactory.battery(inactiveStore)); // excluded

            mockMvc.perform(get("/api/catalog/products/filter"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)));
        }

        @Test
        void filterByCategory_returnsOnlyMatchingType() throws Exception {
            productRepository.save(TestDataFactory.brake(localStore));
            productRepository.save(TestDataFactory.tire(localStore));

            mockMvc.perform(get("/api/catalog/products/filter")
                            .param("categorySlug", "brake"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name").value("Brembo Brake"));
        }

        @Test
        void filterByCity_returnsOnlyMatchingCity() throws Exception {
            productRepository.save(TestDataFactory.brake(localStore));   // Achrafieh
            productRepository.save(TestDataFactory.tire(remoteStore));   // Tripoli

            mockMvc.perform(get("/api/catalog/products/filter")
                            .param("city", "Achrafieh"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].storeCity").value("Achrafieh"));
        }

        @Test
        void filterByCondition() throws Exception {
            Brake usedBrake = TestDataFactory.brake(localStore);
            usedBrake.setCondition(ProductCondition.USED);
            productRepository.save(usedBrake);

            productRepository.save(TestDataFactory.tire(localStore)); // NEW

            mockMvc.perform(get("/api/catalog/products/filter")
                            .param("condition", "USED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)));
        }

        @Test
        void filterByPriceRange() throws Exception {
            productRepository.save(TestDataFactory.brake(localStore));   // 85.00
            productRepository.save(TestDataFactory.battery(localStore)); // 150.00

            mockMvc.perform(get("/api/catalog/products/filter")
                            .param("priceMin", "100")
                            .param("priceMax", "200"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name").value("Varta Battery"));
        }

        @Test
        void filterByBrand() throws Exception {
            productRepository.save(TestDataFactory.brake(localStore));  // Brembo
            productRepository.save(TestDataFactory.tire(localStore));   // Michelin

            mockMvc.perform(get("/api/catalog/products/filter")
                            .param("brand", "Michelin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].brand").value("Michelin"));
        }

        @Test
        void filterBySearchTerm() throws Exception {
            productRepository.save(TestDataFactory.brake(localStore));   // "Brembo Brake"
            productRepository.save(TestDataFactory.tire(localStore));    // "Michelin Tire"

            mockMvc.perform(get("/api/catalog/products/filter")
                            .param("searchTerm", "michelin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].brand").value("Michelin"));
        }

        @Test
        void combinedFilters() throws Exception {
            productRepository.save(TestDataFactory.brake(localStore));   // Brembo, 85, Achrafieh
            productRepository.save(TestDataFactory.tire(localStore));    // Michelin, 120, Achrafieh
            productRepository.save(TestDataFactory.brake(remoteStore));  // Brembo, 85, Tripoli

            mockMvc.perform(get("/api/catalog/products/filter")
                            .param("categorySlug", "brake")
                            .param("city", "Achrafieh")
                            .param("brand", "Brembo"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].storeCity").value("Achrafieh"));
        }

        @Test
        void pagination_respectsSizeParam() throws Exception {
            for (int i = 0; i < 5; i++) {
                Brake b = TestDataFactory.brake(localStore);
                b.setName("Brake " + i);
                b.setPrice(BigDecimal.valueOf(80 + i));
                productRepository.save(b);
            }

            mockMvc.perform(get("/api/catalog/products/filter")
                            .param("page", "0")
                            .param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(5))
                    .andExpect(jsonPath("$.totalPages").value(3));
        }
    }
}
