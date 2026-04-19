package com.example.fixit.module.user.repository;

import com.example.fixit.common.PostgresIntegrationBase;
import com.example.fixit.common.TestDataFactory;
import com.example.fixit.common.config.JpaConfig;
import com.example.fixit.module.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaConfig.class)
class UserRepositoryTest extends PostgresIntegrationBase {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByKeycloakId_returnsUser_whenExists() {
        userRepository.save(TestDataFactory.user());

        Optional<User> found = userRepository.findByKeycloakId("kc-test-001");

        assertTrue(found.isPresent());
        assertEquals("kc-test-001", found.get().getKeycloakId());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void findByKeycloakId_returnsEmpty_whenNotFound() {
        Optional<User> found = userRepository.findByKeycloakId("nonexistent");

        assertTrue(found.isEmpty());
    }

    @Test
    void existsByKeycloakId_returnsTrue_whenExists() {
        userRepository.save(TestDataFactory.user());

        assertTrue(userRepository.existsByKeycloakId("kc-test-001"));
    }

    @Test
    void existsByKeycloakId_returnsFalse_whenNotFound() {
        assertFalse(userRepository.existsByKeycloakId("nonexistent"));
    }

    @Test
    void uniqueConstraint_onKeycloakId_throwsOnDuplicate() {
        userRepository.saveAndFlush(TestDataFactory.user("kc-dup", "a@example.com", "User A"));
        User duplicate = TestDataFactory.user("kc-dup", "b@example.com", "User B");

        assertThrows(DataIntegrityViolationException.class, () ->
                userRepository.saveAndFlush(duplicate));
    }
}
