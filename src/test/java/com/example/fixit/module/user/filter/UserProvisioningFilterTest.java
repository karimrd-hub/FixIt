package com.example.fixit.module.user.filter;

import com.example.fixit.module.user.service.UserProvisioningService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProvisioningFilterTest {

    @Mock
    private UserProvisioningService userProvisioningService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private UserProvisioningFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Jwt buildJwt(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .subject(claims.getOrDefault("sub", "default-sub").toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300));
        claims.forEach((k, v) -> {
            if (!"sub".equals(k)) {
                builder.claim(k, v);
            }
        });
        return builder.build();
    }

    private void setJwtAuthentication(Jwt jwt) {
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    class WhenAuthenticated {

        @Test
        void callsProvisionWithCorrectClaims() throws ServletException, IOException {
            Jwt jwt = buildJwt(Map.of(
                    "sub", "kc-123",
                    "email", "user@example.com",
                    "preferred_username", "johndoe"
            ));
            setJwtAuthentication(jwt);

            filter.doFilterInternal(request, response, filterChain);

            verify(userProvisioningService).provisionIfAbsent("kc-123", "user@example.com", "johndoe");
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    class WhenUnauthenticated {

        @Test
        void skipsProvisioningWhenNoAuthentication() throws ServletException, IOException {
            // SecurityContext is empty

            filter.doFilterInternal(request, response, filterChain);

            verifyNoInteractions(userProvisioningService);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void skipsProvisioningForNonJwtPrincipal() throws ServletException, IOException {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("user", "pass");
            SecurityContextHolder.getContext().setAuthentication(auth);

            filter.doFilterInternal(request, response, filterChain);

            verifyNoInteractions(userProvisioningService);
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    class DisplayNameFallback {

        @Test
        void usesNameWhenPreferredUsernameIsMissing() throws ServletException, IOException {
            Jwt jwt = buildJwt(Map.of(
                    "sub", "kc-456",
                    "email", "user@example.com",
                    "name", "John Doe"
            ));
            setJwtAuthentication(jwt);

            filter.doFilterInternal(request, response, filterChain);

            verify(userProvisioningService).provisionIfAbsent("kc-456", "user@example.com", "John Doe");
        }

        @Test
        void usesSubWhenBothPreferredUsernameAndNameAreMissing() throws ServletException, IOException {
            Jwt jwt = buildJwt(Map.of(
                    "sub", "kc-789",
                    "email", "user@example.com"
            ));
            setJwtAuthentication(jwt);

            filter.doFilterInternal(request, response, filterChain);

            verify(userProvisioningService).provisionIfAbsent("kc-789", "user@example.com", "kc-789");
        }
    }

    @Nested
    class FilterChainContinuation {

        @Test
        void alwaysContinuesFilterChain() throws ServletException, IOException {
            Jwt jwt = buildJwt(Map.of(
                    "sub", "kc-000",
                    "email", "user@example.com",
                    "preferred_username", "testuser"
            ));
            setJwtAuthentication(jwt);
            doThrow(new RuntimeException("provisioning failed"))
                    .when(userProvisioningService).provisionIfAbsent(anyString(), anyString(), anyString());

            filter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
        }
    }
}
