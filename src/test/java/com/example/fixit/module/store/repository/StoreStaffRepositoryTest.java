package com.example.fixit.module.store.repository;

import com.example.fixit.common.PostgresIntegrationBase;
import com.example.fixit.common.TestDataFactory;
import com.example.fixit.common.config.JpaConfig;
import com.example.fixit.module.store.entity.Store;
import com.example.fixit.module.store.entity.StoreRole;
import com.example.fixit.module.store.entity.StoreStaff;
import com.example.fixit.module.store.entity.StoreStaffStatus;
import com.example.fixit.module.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaConfig.class)
class StoreStaffRepositoryTest extends PostgresIntegrationBase {

    @Autowired
    private StoreStaffRepository storeStaffRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private com.example.fixit.module.user.repository.UserRepository userRepository;

    // ── findByUserIdAndStoreId ────────────────────────────────────────────────

    @Test
    void findByUserIdAndStoreId_returnsMatch() {
        User user = userRepository.save(TestDataFactory.user());
        Store store = storeRepository.save(TestDataFactory.store());
        StoreStaff saved = storeStaffRepository.save(
                TestDataFactory.storeStaff(user, store, StoreRole.OWNER));

        Optional<StoreStaff> found =
                storeStaffRepository.findByUserIdAndStoreId(user.getId(), store.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals(StoreRole.OWNER, found.get().getStoreRole());
    }

    @Test
    void findByUserIdAndStoreId_returnsEmpty_whenNoMatch() {
        User user = userRepository.save(TestDataFactory.user());
        Store store = storeRepository.save(TestDataFactory.store());

        Optional<StoreStaff> found =
                storeStaffRepository.findByUserIdAndStoreId(user.getId(), store.getId());

        assertTrue(found.isEmpty());
    }

    // ── findByUserIdAndStatus ────────────────────────────────────────────────

    @Test
    void findByUserIdAndStatus_returnsOnlyActive() {
        User user = userRepository.save(TestDataFactory.user());
        Store activeStore = storeRepository.save(
                TestDataFactory.store("Active Store", "Beirut", "Beirut", "Achrafieh"));
        Store inactiveStore = storeRepository.save(
                TestDataFactory.store("Inactive Store", "Beirut", "Beirut", "Hamra"));

        storeStaffRepository.save(TestDataFactory.storeStaff(
                user, activeStore, StoreRole.EMPLOYEE, StoreStaffStatus.ACTIVE));
        storeStaffRepository.save(TestDataFactory.storeStaff(
                user, inactiveStore, StoreRole.EMPLOYEE, StoreStaffStatus.INACTIVE));

        List<StoreStaff> active =
                storeStaffRepository.findByUserIdAndStatus(user.getId(), StoreStaffStatus.ACTIVE);

        assertEquals(1, active.size());
        assertEquals(activeStore.getId(), active.get(0).getStore().getId());
    }

    // ── findByStoreIdAndStatus ───────────────────────────────────────────────

    @Test
    void findByStoreIdAndStatus_returnsOnlyActive() {
        Store store = storeRepository.save(TestDataFactory.store());
        User activeUser = userRepository.save(
                TestDataFactory.user("kc-active", "active@example.com", "Active User"));
        User inactiveUser = userRepository.save(
                TestDataFactory.user("kc-inactive", "inactive@example.com", "Inactive User"));

        storeStaffRepository.save(TestDataFactory.storeStaff(
                activeUser, store, StoreRole.EMPLOYEE, StoreStaffStatus.ACTIVE));
        storeStaffRepository.save(TestDataFactory.storeStaff(
                inactiveUser, store, StoreRole.EMPLOYEE, StoreStaffStatus.INACTIVE));

        List<StoreStaff> active =
                storeStaffRepository.findByStoreIdAndStatus(store.getId(), StoreStaffStatus.ACTIVE);

        assertEquals(1, active.size());
        assertEquals(activeUser.getId(), active.get(0).getUser().getId());
    }

    // ── existsByUserIdAndStoreIdAndStoreRoleIn ───────────────────────────────

    @Test
    void existsByUserIdAndStoreIdAndStoreRoleIn_trueWhenRoleMatches() {
        User user = userRepository.save(TestDataFactory.user());
        Store store = storeRepository.save(TestDataFactory.store());
        storeStaffRepository.save(TestDataFactory.storeStaff(user, store, StoreRole.OWNER));

        boolean exists = storeStaffRepository.existsByUserIdAndStoreIdAndStoreRoleIn(
                user.getId(), store.getId(), Set.of(StoreRole.OWNER, StoreRole.EMPLOYEE));

        assertTrue(exists);
    }

    @Test
    void existsByUserIdAndStoreIdAndStoreRoleIn_falseWhenRoleDoesNotMatch() {
        User user = userRepository.save(TestDataFactory.user());
        Store store = storeRepository.save(TestDataFactory.store());
        storeStaffRepository.save(TestDataFactory.storeStaff(user, store, StoreRole.EMPLOYEE));

        boolean exists = storeStaffRepository.existsByUserIdAndStoreIdAndStoreRoleIn(
                user.getId(), store.getId(), Set.of(StoreRole.OWNER));

        assertFalse(exists);
    }

    // ── Unique constraint (user_id, store_id) ────────────────────────────────

    @Test
    void uniqueConstraint_onUserAndStore_throwsOnDuplicate() {
        User user = userRepository.save(TestDataFactory.user());
        Store store = storeRepository.save(TestDataFactory.store());
        storeStaffRepository.saveAndFlush(
                TestDataFactory.storeStaff(user, store, StoreRole.OWNER));

        StoreStaff duplicate = TestDataFactory.storeStaff(user, store, StoreRole.EMPLOYEE);

        assertThrows(DataIntegrityViolationException.class, () ->
                storeStaffRepository.saveAndFlush(duplicate));
    }
}
