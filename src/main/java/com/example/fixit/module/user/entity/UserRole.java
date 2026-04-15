package com.example.fixit.module.user.entity;

/**
 * Platform-level roles managed by Keycloak.
 * Not persisted on the User entity — used for code references
 * (e.g., @PreAuthorize checks, role constants).
 */
public enum UserRole {
    BUYER,
    SELLER,
    ADMIN
}
