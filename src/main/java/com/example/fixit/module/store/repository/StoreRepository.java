package com.example.fixit.module.store.repository;

import com.example.fixit.module.store.entity.Store;
import com.example.fixit.module.store.entity.StoreStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findByStatusAndCity(StoreStatus status, String city);

    List<Store> findByStatusAndCityNot(StoreStatus status, String city);
}