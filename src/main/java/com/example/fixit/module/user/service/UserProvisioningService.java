package com.example.fixit.module.user.service;

import com.example.fixit.module.user.entity.User;
import com.example.fixit.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProvisioningService {

    private final UserRepository userRepository;

    @Transactional
    public void provisionIfAbsent(String keycloakId, String email, String displayName) {
        if (userRepository.existsByKeycloakId(keycloakId)) {
            return;
        }

        User user = new User();
        user.setKeycloakId(keycloakId);
        user.setEmail(email);
        user.setDisplayName(displayName);

        try {
            userRepository.save(user);
            log.info("Provisioned new user: keycloakId={}", keycloakId);
        } catch (DataIntegrityViolationException e) {
            // concurrent first request — user was already created by another thread
            log.debug("User already provisioned by concurrent request: keycloakId={}", keycloakId);
        }
    }
}
