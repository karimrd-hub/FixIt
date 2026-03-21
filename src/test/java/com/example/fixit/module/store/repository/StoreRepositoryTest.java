package com.example.fixit.module.store.repository;

import com.example.fixit.common.TestDataFactory;
import com.example.fixit.common.config.JpaConfig;
import com.example.fixit.module.store.entity.Store;
import com.example.fixit.module.store.entity.StoreStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class) // because the JpaAuditing attributes are null when we test the jpa slice

class StoreRepositoryTest {

    @Autowired
    private StoreRepository storeRepository;

    // ── Save & find ───────────────────────────────────────────────────────────

    @Test
    void saveStore_thenFindById() {
        Store saved = storeRepository.save(TestDataFactory.store());

        Optional<Store> found = storeRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Test Store", found.get().getName());
    }

    @Test
    void idIsGeneratedOnSave() {
        Store saved = storeRepository.save(TestDataFactory.store());

        assertNotNull(saved.getId());
    }

    @Test
    void auditFieldsArePopulatedOnSave() {
        Store saved = storeRepository.save(TestDataFactory.store());

        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    // ── Custom query methods ──────────────────────────────────────────────────

    @Test
    void findByStatusAndCity_returnsMatchingStores() {
        storeRepository.save(TestDataFactory.store("Store A", "Beirut", "Beirut", "Achrafieh"));
        storeRepository.save(TestDataFactory.store("Store B", "Beirut", "Beirut", "Achrafieh"));
        storeRepository.save(TestDataFactory.store("Store C", "Mount Lebanon", "Matn", "Jdeideh"));

        List<Store> result = storeRepository.findByStatusAndCity(StoreStatus.ACTIVE, "Achrafieh");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> s.getCity().equals("Achrafieh")));
    }

    @Test
    void findByStatusAndCity_returnsEmptyWhenNoneMatch() {
        storeRepository.save(TestDataFactory.store("Store A", "Beirut", "Beirut", "Achrafieh"));

        List<Store> result = storeRepository.findByStatusAndCity(StoreStatus.ACTIVE, "Tripoli");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByStatusAndCityNot_excludesGivenCity() {
        storeRepository.save(TestDataFactory.store("Store A", "Beirut", "Beirut", "Achrafieh"));
        storeRepository.save(TestDataFactory.store("Store B", "Mount Lebanon", "Matn", "Jdeideh"));
        storeRepository.save(TestDataFactory.store("Store C", "North", "Tripoli", "Tripoli"));

        List<Store> result = storeRepository.findByStatusAndCityNot(StoreStatus.ACTIVE, "Achrafieh");

        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(s -> s.getCity().equals("Achrafieh")));
    }

    @Test
    void findByStatusAndCityNot_inactiveStoreIsExcluded() {
        Store inactive = TestDataFactory.store("Inactive Store", "Beirut", "Beirut", "Hamra");
        inactive.setStatus(StoreStatus.INACTIVE);
        storeRepository.save(inactive);
        storeRepository.save(TestDataFactory.store("Active Store", "Beirut", "Beirut", "Achrafieh"));

        // looking for ACTIVE stores NOT in Hamra — inactive store should not appear
        List<Store> result = storeRepository.findByStatusAndCityNot(StoreStatus.ACTIVE, "Hamra");

        assertEquals(1, result.size());
        assertEquals("Active Store", result.get(0).getName());
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Test
    void deleteStore_thenNotFound() {
        Store saved = storeRepository.save(TestDataFactory.store());
        Long id = saved.getId();

        storeRepository.deleteById(id);

        assertFalse(storeRepository.findById(id).isPresent());
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsAllSavedStores() {
        storeRepository.save(TestDataFactory.store("Store A", "Beirut", "Beirut", "Achrafieh"));
        storeRepository.save(TestDataFactory.store("Store B", "North", "Tripoli", "Tripoli"));

        List<Store> all = storeRepository.findAll();

        assertEquals(2, all.size());
    }
}
