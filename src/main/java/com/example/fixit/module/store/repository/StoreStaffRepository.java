package com.example.fixit.module.store.repository;

import com.example.fixit.module.store.entity.StoreRole;
import com.example.fixit.module.store.entity.StoreStaff;
import com.example.fixit.module.store.entity.StoreStaffStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StoreStaffRepository extends JpaRepository<StoreStaff, Long> {

    Optional<StoreStaff> findByUserIdAndStoreId(Long userId, Long storeId);

    List<StoreStaff> findByUserIdAndStatus(Long userId, StoreStaffStatus status);

    List<StoreStaff> findByStoreIdAndStatus(Long storeId, StoreStaffStatus status);

    boolean existsByUserIdAndStoreIdAndStoreRoleIn(Long userId, Long storeId, Collection<StoreRole> roles);
}
