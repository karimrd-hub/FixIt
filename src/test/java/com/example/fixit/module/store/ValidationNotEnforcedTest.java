package com.example.fixit.module.store;

import com.example.fixit.common.config.JpaConfig;
import com.example.fixit.module.store.entity.FullfillmentMode;
import com.example.fixit.module.store.entity.Store;
import com.example.fixit.module.store.entity.StoreServiceType;
import com.example.fixit.module.store.entity.StoreStatus;
import com.example.fixit.module.store.repository.StoreRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves that @DecimalMin / @DecimalMax on Store.rating ARE enforced
 * now that spring-boot-starter-validation is on the classpath.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class ValidationNotEnforcedTest {

    @Autowired
    private StoreRepository storeRepository;

    @Test
    void ratingAboveMax_throwsConstraintViolation() {
        Store store = new Store();
        store.setName("Test Store");
        store.setDescription("Test");
        store.setPhone("+961 70 123456");
        store.setEmail("test@test.lb");
        store.setAddress("123 Main St");
        store.setCity("Beirut");
        store.setDistrict("Beirut");
        store.setGovernorate("Beirut");
        store.setStatus(StoreStatus.ACTIVE);
        store.setServiceType(StoreServiceType.AUTOPARTS_ONLY);
        store.setFulfillmentMode(FullfillmentMode.IN_SHOP);
        store.setRating(new BigDecimal("9.9")); // above @DecimalMax("5.0")

        assertThatThrownBy(() -> storeRepository.saveAndFlush(store))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
