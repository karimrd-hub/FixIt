package com.example.fixit.common.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class KeycloakJwtConverterTest {

    private JwtAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new SecurityConfig().keycloakJwtConverter();
    }

    private Jwt buildJwt(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .subject("kc-test")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300));
        claims.forEach(builder::claim);
        return builder.build();
    }

    private Set<String> authorityNames(Jwt jwt) {
        AbstractAuthenticationToken token = converter.convert(jwt);
        return token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    @Nested
    class RealmRoles {

        @Test
        void mapsRealmRolesToRolePrefixedAuthorities() {
            Jwt jwt = buildJwt(Map.of(
                    "realm_access", Map.of("roles", List.of("BUYER", "SELLER"))
            ));

            assertThat(authorityNames(jwt)).containsExactlyInAnyOrder("ROLE_BUYER", "ROLE_SELLER");
        }
    }

    @Nested
    class Scopes {

        @Test
        void mapsScopeClaimToScopePrefixedAuthorities() {
            Jwt jwt = buildJwt(Map.of("scope", "read write"));

            assertThat(authorityNames(jwt)).containsExactlyInAnyOrder("SCOPE_read", "SCOPE_write");
        }

        @Test
        void combinesScopesAndRealmRoles() {
            Jwt jwt = buildJwt(Map.of(
                    "scope", "read",
                    "realm_access", Map.of("roles", List.of("BUYER"))
            ));

            assertThat(authorityNames(jwt)).containsExactlyInAnyOrder("SCOPE_read", "ROLE_BUYER");
        }
    }

    @Nested
    class MissingOrMalformedClaims {

        @Test
        void noRealmAccessClaim_producesNoRoleAuthorities() {
            Jwt jwt = buildJwt(Map.of("scope", "read"));

            assertThat(authorityNames(jwt))
                    .allSatisfy(a -> assertThat(a).doesNotStartWith("ROLE_"));
        }

        @Test
        void realmAccessWithoutRolesKey_producesNoRoleAuthorities() {
            Jwt jwt = buildJwt(Map.of("realm_access", Map.of("other", "value")));

            assertThat(authorityNames(jwt))
                    .allSatisfy(a -> assertThat(a).doesNotStartWith("ROLE_"));
        }

        @Test
        void rolesClaimIsString_skipsGracefullyWithNoException() {
            Jwt jwt = buildJwt(Map.of("realm_access", Map.of("roles", "BUYER")));

            assertThatCode(() -> converter.convert(jwt)).doesNotThrowAnyException();
            assertThat(authorityNames(jwt))
                    .allSatisfy(a -> assertThat(a).doesNotStartWith("ROLE_"));
        }

        @Test
        void rolesListContainsNonStringElement_skipsBadEntryKeepsGoodOnes() {
            Jwt jwt = buildJwt(Map.of(
                    "realm_access", Map.of("roles", List.of("BUYER", 42, "SELLER"))
            ));

            assertThatCode(() -> converter.convert(jwt)).doesNotThrowAnyException();
            assertThat(authorityNames(jwt)).containsExactlyInAnyOrder("ROLE_BUYER", "ROLE_SELLER");
        }
    }
}
