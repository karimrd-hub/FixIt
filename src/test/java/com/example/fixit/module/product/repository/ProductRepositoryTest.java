package com.example.fixit.module.product.repository;

import com.example.fixit.common.TestDataFactory;
import com.example.fixit.common.config.JpaConfig;
import com.example.fixit.module.product.autoparts.battery.entity.Battery;
import com.example.fixit.module.product.autoparts.brake.entity.Brake;
import com.example.fixit.module.product.entity.Product;
import com.example.fixit.module.product.entity.ProductCondition;
import com.example.fixit.module.product.entity.ProductStatus;
import com.example.fixit.module.product.autoparts.tire.entity.Tire;
import com.example.fixit.module.store.entity.Store;
import com.example.fixit.module.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class) // because the JpaAuditing attributes are null when we test the jpa slice

class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    private Store store;

    @BeforeEach
    void setUp() {
        store = storeRepository.save(TestDataFactory.store());
    }

    // ── Save & find ───────────────────────────────────────────────────────────

    @Test
    void saveBrake_thenFindById() {
        Brake brake = productRepository.save(TestDataFactory.brake(store));

        Optional<Product> found = productRepository.findById(brake.getId());

        assertTrue(found.isPresent());
        assertInstanceOf(Brake.class, found.get());
        assertEquals("Brembo", found.get().getBrand());
    }

    @Test
    void saveTire_thenFindById() {
        Tire tire = productRepository.save(TestDataFactory.tire(store));

        Optional<Product> found = productRepository.findById(tire.getId());

        assertTrue(found.isPresent());
        assertInstanceOf(Tire.class, found.get());
    }

    @Test
    void saveBattery_thenFindById() {
        Battery battery = productRepository.save(TestDataFactory.battery(store));

        Optional<Product> found = productRepository.findById(battery.getId());

        assertTrue(found.isPresent());
        assertInstanceOf(Battery.class, found.get());
    }

    // ── Inheritance (joined table) ────────────────────────────────────────────

    @Test
    void allSubtypesAreReturnedByFindAll() {
        productRepository.save(TestDataFactory.brake(store));
        productRepository.save(TestDataFactory.tire(store));
        productRepository.save(TestDataFactory.battery(store));
        productRepository.save(TestDataFactory.oilFilter(store));
        productRepository.save(TestDataFactory.airFilter(store));

        List<Product> all = productRepository.findAll();

        assertEquals(5, all.size());
    }

    @Test
    void subtypeFieldsArePersistedCorrectly() {
        Brake saved = productRepository.save(TestDataFactory.brake(store));

        Brake found = (Brake) productRepository.findById(saved.getId()).orElseThrow();

        assertEquals(300, found.getDiameterMm());
        assertEquals("FRONT", found.getAxlePosition());
        assertNotNull(found.getBrakeType());
    }

    // ── Status & condition ────────────────────────────────────────────────────

    @Test
    void productDefaultStatusIsActive() {
        Product saved = productRepository.save(TestDataFactory.brake(store));

        assertEquals(ProductStatus.ACTIVE, saved.getStatus());
    }

    @Test
    void productConditionIsPersistedCorrectly() {
        Brake brake = TestDataFactory.brake(store);
        brake.setCondition(ProductCondition.USED);
        Product saved = productRepository.save(brake);

        assertEquals(ProductCondition.USED, saved.getCondition());
    }

    // ── Store relationship ────────────────────────────────────────────────────

    @Test
    void productIsLinkedToStore() {
        Product saved = productRepository.save(TestDataFactory.brake(store));

        Product found = productRepository.findById(saved.getId()).orElseThrow();

        assertEquals(store.getId(), found.getStore().getId());
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Test
    void deleteProduct_thenNotFound() {
        Product saved = productRepository.save(TestDataFactory.brake(store));
        Long id = saved.getId();

        productRepository.deleteById(id);

        assertFalse(productRepository.findById(id).isPresent());
    }

    // ── Price ─────────────────────────────────────────────────────────────────

    @Test
    void priceIsPersistedWithCorrectScale() {
        Brake brake = TestDataFactory.brake(store);
        brake.setPrice(new BigDecimal("99.99"));
        Product saved = productRepository.save(brake);

        assertEquals(new BigDecimal("99.99"), saved.getPrice());
    }
}
