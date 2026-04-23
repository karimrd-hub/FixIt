package com.example.fixit.common.config;

import com.example.fixit.common.PostgresIntegrationBase;
import com.example.fixit.common.TestDataFactory;
import com.example.fixit.module.store.entity.Store;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(JpaConfig.class)
class AuditorAwareTest extends PostgresIntegrationBase {

    @Autowired
    private TestEntityManager entityManager;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatedSave_populatesCreatedByFromJwtSubject() {
        authenticateAsJwt("kc-123");

        Store saved = persistAndFlush(TestDataFactory.store());

        assertEquals("kc-123", saved.getCreatedBy());
        assertEquals("kc-123", saved.getLastModifiedBy());
    }

    @Test
    void unauthenticatedSave_fallsBackToSystem() {
        SecurityContextHolder.clearContext();

        Store saved = persistAndFlush(TestDataFactory.store());

        assertEquals("system", saved.getCreatedBy());
        assertEquals("system", saved.getLastModifiedBy());
    }

    @Test
    void updateUnderDifferentPrincipal_changesLastModifiedByOnly() {
        authenticateAsJwt("kc-creator");
        Store saved = persistAndFlush(TestDataFactory.store());
        Long id = saved.getId();
        entityManager.clear();

        authenticateAsJwt("kc-editor");
        Store loaded = entityManager.find(Store.class, id);
        loaded.setDescription("updated by editor");
        entityManager.flush();

        assertEquals("kc-creator", loaded.getCreatedBy());
        assertEquals("kc-editor", loaded.getLastModifiedBy());
    }

    @Test
    void nonJwtPrincipal_fallsBackToSystem() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "alice", "pw", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        Store saved = persistAndFlush(TestDataFactory.store());

        assertEquals("system", saved.getCreatedBy());
        assertEquals("system", saved.getLastModifiedBy());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Store persistAndFlush(Store store) {
        Store persisted = entityManager.persistAndFlush(store);
        entityManager.clear();
        return entityManager.find(Store.class, persisted.getId());
    }

    private void authenticateAsJwt(String subject) {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(subject)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("sub", subject)
                .build();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
