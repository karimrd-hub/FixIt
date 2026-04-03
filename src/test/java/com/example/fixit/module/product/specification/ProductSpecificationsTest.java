package com.example.fixit.module.product.specification;

import com.example.fixit.common.TestDataFactory;
import com.example.fixit.common.config.JpaConfig;
import com.example.fixit.module.catalog.dto.ProductFilterDTO;
import com.example.fixit.module.product.autoparts.battery.entity.Battery;
import com.example.fixit.module.product.autoparts.brake.entity.Brake;
import com.example.fixit.module.product.autoparts.tire.entity.Tire;
import com.example.fixit.module.product.entity.Product;
import com.example.fixit.module.product.entity.ProductCondition;
import com.example.fixit.module.product.entity.ProductStatus;
import com.example.fixit.module.product.repository.ProductRepository;
import com.example.fixit.module.store.entity.FullfillmentMode;
import com.example.fixit.module.store.entity.Store;
import com.example.fixit.module.store.entity.StoreServiceType;
import com.example.fixit.module.store.entity.StoreStatus;
import com.example.fixit.module.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class ProductSpecificationsTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    private Store activeStore;
    private Store inactiveStore;

    @BeforeEach
    void setUp() {
        activeStore = TestDataFactory.store("Active Store", "Beirut", "Beirut", "Achrafieh");
        activeStore = storeRepository.save(activeStore);

        inactiveStore = TestDataFactory.store("Inactive Store", "Beirut", "Beirut", "Hamra");
        inactiveStore.setStatus(StoreStatus.INACTIVE);
        inactiveStore = storeRepository.save(inactiveStore);
    }

    // ── byCategory ────────────────────────────────────────────────────────────

    @Nested
    class ByCategoryTests {

        @Test
        void returnsOnlyBrakes() {
            productRepository.save(TestDataFactory.brake(activeStore));
            productRepository.save(TestDataFactory.tire(activeStore));

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.byCategory(Brake.class)));

            assertEquals(1, results.size());
            assertInstanceOf(Brake.class, results.get(0));
        }

        @Test
        void returnsOnlyTires() {
            productRepository.save(TestDataFactory.brake(activeStore));
            productRepository.save(TestDataFactory.tire(activeStore));

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.byCategory(Tire.class)));

            assertEquals(1, results.size());
            assertInstanceOf(Tire.class, results.get(0));
        }

        @Test
        void returnsEmptyWhenNoCategoryMatch() {
            productRepository.save(TestDataFactory.brake(activeStore));

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.byCategory(Battery.class)));

            assertTrue(results.isEmpty());
        }
    }

    // ── storeIsActive ─────────────────────────────────────────────────────────

    @Nested
    class StoreIsActiveTests {

        @Test
        void returnsProductsFromActiveStoresOnly() {
            productRepository.save(TestDataFactory.brake(activeStore));
            productRepository.save(TestDataFactory.tire(inactiveStore));

            List<Product> results = productRepository.findAll(ProductSpecifications.storeIsActive());

            assertEquals(1, results.size());
            assertEquals(activeStore.getId(), results.get(0).getStore().getId());
        }

        @Test
        void returnsEmptyWhenAllStoresInactive() {
            productRepository.save(TestDataFactory.brake(inactiveStore));

            List<Product> results = productRepository.findAll(ProductSpecifications.storeIsActive());

            assertTrue(results.isEmpty());
        }
    }

    // ── byStatus ──────────────────────────────────────────────────────────────

    @Nested
    class ByStatusTests {

        @Test
        void returnsNullWhenStatusIsNull() {
            assertNull(ProductSpecifications.byStatus(null));
        }

        @Test
        void filtersActiveProducts() {
            Brake active = TestDataFactory.brake(activeStore);
            active.setStatus(ProductStatus.ACTIVE);
            productRepository.save(active);

            Brake discontinued = TestDataFactory.brake(activeStore);
            discontinued.setStatus(ProductStatus.DISCONTINUED);
            productRepository.save(discontinued);

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.byStatus(ProductStatus.ACTIVE)));

            assertEquals(1, results.size());
            assertEquals(ProductStatus.ACTIVE, results.get(0).getStatus());
        }
    }

    // ── byCondition ───────────────────────────────────────────────────────────

    @Nested
    class ByConditionTests {

        @Test
        void returnsNullWhenConditionIsNull() {
            assertNull(ProductSpecifications.byCondition(null));
        }

        @Test
        void filtersUsedProducts() {
            Brake newBrake = TestDataFactory.brake(activeStore);
            newBrake.setCondition(ProductCondition.NEW);
            productRepository.save(newBrake);

            Brake usedBrake = TestDataFactory.brake(activeStore);
            usedBrake.setCondition(ProductCondition.USED);
            productRepository.save(usedBrake);

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.byCondition(ProductCondition.USED)));

            assertEquals(1, results.size());
            assertEquals(ProductCondition.USED, results.get(0).getCondition());
        }
    }

    // ── byGovernorate ─────────────────────────────────────────────────────────

    @Nested
    class ByGovernorateTests {

        @Test
        void returnsNullWhenGovernorateIsNull() {
            assertNull(ProductSpecifications.byGovernorate(null));
        }

        @Test
        void returnsNullWhenGovernorateIsBlank() {
            assertNull(ProductSpecifications.byGovernorate("  "));
        }

        @Test
        void filtersCaseInsensitive() {
            productRepository.save(TestDataFactory.brake(activeStore)); // Beirut governorate

            Store northStore = storeRepository.save(
                    TestDataFactory.store("North Store", "North", "Tripoli", "Tripoli"));
            productRepository.save(TestDataFactory.tire(northStore));

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.byGovernorate("beirut")));

            assertEquals(1, results.size());
        }
    }

    // ── byDistrict ────────────────────────────────────────────────────────────

    @Nested
    class ByDistrictTests {

        @Test
        void returnsNullWhenDistrictIsNull() {
            assertNull(ProductSpecifications.byDistrict(null));
        }

        @Test
        void filtersByDistrict() {
            productRepository.save(TestDataFactory.brake(activeStore)); // Beirut district

            Store matnStore = storeRepository.save(
                    TestDataFactory.store("Matn Store", "Mount Lebanon", "Matn", "Jdeideh"));
            productRepository.save(TestDataFactory.tire(matnStore));

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.byDistrict("Matn")));

            assertEquals(1, results.size());
        }
    }

    // ── byCity ────────────────────────────────────────────────────────────────

    @Nested
    class ByCityTests {

        @Test
        void returnsNullWhenCityIsNull() {
            assertNull(ProductSpecifications.byCity(null));
        }

        @Test
        void returnsNullWhenCityIsBlank() {
            assertNull(ProductSpecifications.byCity(""));
        }

        @Test
        void filtersCaseInsensitive() {
            productRepository.save(TestDataFactory.brake(activeStore)); // Achrafieh

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.byCity("achrafieh")));

            assertEquals(1, results.size());
        }
    }

    // ── byCityExcluding ───────────────────────────────────────────────────────

    @Nested
    class ByCityExcludingTests {

        @Test
        void returnsNullWhenCityIsNull() {
            assertNull(ProductSpecifications.byCityExcluding(null));
        }

        @Test
        void excludesGivenCity() {
            productRepository.save(TestDataFactory.brake(activeStore)); // Achrafieh

            Store otherStore = storeRepository.save(
                    TestDataFactory.store("Other Store", "North", "Tripoli", "Tripoli"));
            productRepository.save(TestDataFactory.tire(otherStore));

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.byCityExcluding("Achrafieh")));

            assertEquals(1, results.size());
            assertInstanceOf(Tire.class, results.get(0));
        }
    }

    // ── byFulfillmentMode ─────────────────────────────────────────────────────

    @Nested
    class ByFulfillmentModeTests {

        @Test
        void returnsNullWhenModeIsNull() {
            assertNull(ProductSpecifications.byFulfillmentMode(null));
        }

        @Test
        void filtersByFulfillmentMode() {
            productRepository.save(TestDataFactory.brake(activeStore)); // IN_SHOP

            Store deliveryStore = TestDataFactory.store("Delivery Store", "Beirut", "Beirut", "Hamra");
            deliveryStore.setFulfillmentMode(FullfillmentMode.DELIVERY);
            deliveryStore = storeRepository.save(deliveryStore);
            productRepository.save(TestDataFactory.tire(deliveryStore));

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.byFulfillmentMode(FullfillmentMode.DELIVERY)));

            assertEquals(1, results.size());
        }
    }

    // ── byServiceType ─────────────────────────────────────────────────────────

    @Nested
    class ByServiceTypeTests {

        @Test
        void returnsNullWhenServiceTypeIsNull() {
            assertNull(ProductSpecifications.byServiceType(null));
        }

        @Test
        void filtersByServiceType() {
            productRepository.save(TestDataFactory.brake(activeStore)); // AUTOPARTS_ONLY

            Store repairStore = TestDataFactory.store("Repair Store", "Beirut", "Beirut", "Hamra");
            repairStore.setServiceType(StoreServiceType.AUTOPARTS_AND_REPAIR);
            repairStore = storeRepository.save(repairStore);
            productRepository.save(TestDataFactory.tire(repairStore));

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.byServiceType(StoreServiceType.AUTOPARTS_AND_REPAIR)));

            assertEquals(1, results.size());
        }
    }

    // ── byBrand ───────────────────────────────────────────────────────────────

    @Nested
    class ByBrandTests {

        @Test
        void returnsNullWhenBrandIsNull() {
            assertNull(ProductSpecifications.byBrand(null));
        }

        @Test
        void returnsNullWhenBrandIsBlank() {
            assertNull(ProductSpecifications.byBrand(""));
        }

        @Test
        void filtersCaseInsensitive() {
            productRepository.save(TestDataFactory.brake(activeStore)); // Brembo
            productRepository.save(TestDataFactory.tire(activeStore));  // Michelin

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.byBrand("brembo")));

            assertEquals(1, results.size());
            assertEquals("Brembo", results.get(0).getBrand());
        }
    }

    // ── byPriceRange ──────────────────────────────────────────────────────────

    @Nested
    class ByPriceRangeTests {

        @Test
        void returnsNullWhenBothBoundsNull() {
            assertNull(ProductSpecifications.byPriceRange(null, null));
        }

        @Test
        void filtersWithMinOnly() {
            productRepository.save(TestDataFactory.brake(activeStore));   // 85.00
            productRepository.save(TestDataFactory.battery(activeStore)); // 150.00

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.byPriceRange(BigDecimal.valueOf(100), null)));

            assertEquals(1, results.size());
            assertInstanceOf(Battery.class, results.get(0));
        }

        @Test
        void filtersWithMaxOnly() {
            productRepository.save(TestDataFactory.brake(activeStore));   // 85.00
            productRepository.save(TestDataFactory.battery(activeStore)); // 150.00

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.byPriceRange(null, BigDecimal.valueOf(100))));

            assertEquals(1, results.size());
            assertInstanceOf(Brake.class, results.get(0));
        }

        @Test
        void filtersWithBothBounds() {
            productRepository.save(TestDataFactory.brake(activeStore));   // 85.00
            productRepository.save(TestDataFactory.tire(activeStore));    // 120.00
            productRepository.save(TestDataFactory.battery(activeStore)); // 150.00

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.byPriceRange(
                            BigDecimal.valueOf(80), BigDecimal.valueOf(130))));

            assertEquals(2, results.size());
        }
    }

    // ── bySearchTerm ──────────────────────────────────────────────────────────

    @Nested
    class BySearchTermTests {

        @Test
        void returnsNullWhenTermIsNull() {
            assertNull(ProductSpecifications.bySearchTerm(null));
        }

        @Test
        void returnsNullWhenTermIsBlank() {
            assertNull(ProductSpecifications.bySearchTerm("  "));
        }

        @Test
        void matchesByName() {
            productRepository.save(TestDataFactory.brake(activeStore));  // "Brembo Brake"
            productRepository.save(TestDataFactory.tire(activeStore));   // "Michelin Tire"

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.bySearchTerm("brembo")));

            assertEquals(1, results.size());
        }

        @Test
        void matchesByBrand() {
            productRepository.save(TestDataFactory.battery(activeStore)); // brand: "Varta"

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.bySearchTerm("varta")));

            assertEquals(1, results.size());
        }

        @Test
        void matchesByDescription() {
            productRepository.save(TestDataFactory.brake(activeStore)); // description contains "Brembo"

            List<Product> results = productRepository.findAll(
                    Specification.where(ProductSpecifications.bySearchTerm("test product")));

            assertEquals(1, results.size());
        }
    }

    // ── fromFilter (composite) ────────────────────────────────────────────────

    @Nested
    class FromFilterTests {

        @Test
        void emptyFilterReturnsAllActiveStoreProducts() {
            productRepository.save(TestDataFactory.brake(activeStore));
            productRepository.save(TestDataFactory.tire(activeStore));
            productRepository.save(TestDataFactory.battery(inactiveStore));

            ProductFilterDTO filter = new ProductFilterDTO();
            List<Product> results = productRepository.findAll(ProductSpecifications.fromFilter(filter));

            assertEquals(2, results.size());
        }

        @Test
        void combinesCategoryAndCity() {
            productRepository.save(TestDataFactory.brake(activeStore));
            productRepository.save(TestDataFactory.tire(activeStore));

            Store otherStore = storeRepository.save(
                    TestDataFactory.store("Other", "North", "Tripoli", "Tripoli"));
            productRepository.save(TestDataFactory.brake(otherStore));

            ProductFilterDTO filter = new ProductFilterDTO();
            filter.setCategorySlug("brake");
            filter.setCity("Achrafieh");

            List<Product> results = productRepository.findAll(ProductSpecifications.fromFilter(filter));

            assertEquals(1, results.size());
            assertInstanceOf(Brake.class, results.get(0));
        }

        @Test
        void combinesMultipleFilters() {
            Brake expensiveBrake = TestDataFactory.brake(activeStore);
            expensiveBrake.setPrice(BigDecimal.valueOf(200));
            expensiveBrake.setCondition(ProductCondition.USED);
            productRepository.save(expensiveBrake);

            productRepository.save(TestDataFactory.brake(activeStore)); // 85.00, NEW

            ProductFilterDTO filter = new ProductFilterDTO();
            filter.setCategorySlug("brake");
            filter.setCondition(ProductCondition.USED);
            filter.setPriceMin(BigDecimal.valueOf(100));

            List<Product> results = productRepository.findAll(ProductSpecifications.fromFilter(filter));

            assertEquals(1, results.size());
            assertEquals(ProductCondition.USED, results.get(0).getCondition());
        }
    }
}
