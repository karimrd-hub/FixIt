package com.example.fixit.module.user.filter;

import com.example.fixit.module.user.service.UserProvisioningService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UserProvisioningFilter extends OncePerRequestFilter {

    private final UserProvisioningService userProvisioningService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt jwt) {

            String keycloakId = jwt.getSubject();
            String email = jwt.getClaimAsString("email");
            String displayName = resolveDisplayName(jwt, keycloakId);

            // Retry policy on transient DB errors lives in UserProvisioningService (@Retryable).
            // Anything still thrown after retries is terminal — propagate and let Spring surface a 500.
            userProvisioningService.provisionIfAbsent(keycloakId, email, displayName);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveDisplayName(Jwt jwt, String fallback) {
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }
        String name = jwt.getClaimAsString("name");
        if (name != null && !name.isBlank()) {
            return name;
        }
        return fallback;
    }
}
