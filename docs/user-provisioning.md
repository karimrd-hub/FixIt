# User Auto-Provisioning & Store Staff — Implementation Approach

## Overview

When Keycloak authenticates a user, the backend needs a local `User` record as an FK anchor for Cart, Order, StoreStaff, etc. Rather than requiring a separate registration endpoint, the system auto-provisions the `User` record on the first authenticated API request.

This document covers the provisioning mechanism, the StoreStaff relationship model, and the Liquibase migration.

---

## Auto-Provisioning: `OncePerRequestFilter`

### Why a Filter Over an Interceptor

| Concern | `OncePerRequestFilter` | `HandlerInterceptor` |
|---|---|---|
| Execution point | Before Spring MVC dispatch | After dispatch, before controller |
| Availability for `@PreAuthorize` | Yes — user exists before security checks | No — may not exist yet |
| Covers non-MVC endpoints (actuator, etc.) | Yes | No |
| Standard in Keycloak-backed apps | Yes | Rarely used for this |

A servlet filter runs earlier in the chain, guaranteeing the `User` record exists by the time any controller, `@PreAuthorize`, or `AuditorAware` needs it.

### Filter Design: `UserProvisioningFilter`

**Location:** `com.example.fixit.module.user.filter.UserProvisioningFilter`

**Behavior:**

1. Runs once per request, after the OAuth2 resource server filter (JWT is already validated)
2. Reads `SecurityContextHolder.getContext().getAuthentication()`
3. If the principal is not a `Jwt`, skips (anonymous/public request)
4. Extracts `sub` claim → calls `userRepository.existsByKeycloakId(sub)`
5. If `false`, creates a `User` from JWT claims:
   - `keycloakId` ← `sub`
   - `email` ← `email` claim
   - `displayName` ← `preferred_username` (fallback to `name`, then `sub`)
6. Persists via `UserRepository.save()`
7. Continues the filter chain

**Key properties:**

- **Idempotent** — `existsByKeycloakId` is a simple indexed boolean check; no heavy work on repeat requests
- **Fast path** — 99%+ of requests hit the `existsByKeycloakId = true` branch (single indexed query)
- **Transactional** — the provisioning logic (check + create) runs in a `@Transactional` method on a helper service, not on the filter itself, keeping the transaction scope tight
- **Graceful** — silently skips unauthenticated requests (catalog browsing)
- **Race-safe** — unique constraint on `keycloak_id` + catch `DataIntegrityViolationException` handles the rare case of two concurrent first requests from the same user

### Filter Registration

Registered in `SecurityConfig` via `addFilterAfter(userProvisioningFilter, BearerTokenAuthenticationFilter.class)` so it runs right after JWT validation.

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http,
                                       UserProvisioningFilter provisioningFilter) throws Exception {
    http
        // ... existing config ...
        .addFilterAfter(provisioningFilter, BearerTokenAuthenticationFilter.class);
    return http.build();
}
```

### Provisioning Service

The filter delegates to a `UserProvisioningService` to keep the filter thin and the transaction boundary clean:

```java
@Service
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
        } catch (DataIntegrityViolationException e) {
            // concurrent first request — user was created by another thread
        }
    }
}
```

---

## StoreStaff Entity

The `StoreStaff` entity models the many-to-many relationship between `User` and `Store`, with a role qualifier. This is documented in [user-roles.md](user-roles.md) — key points repeated here for implementation reference.

### Entity Design

**Location:** `com.example.fixit.module.store.entity.StoreStaff`

```
StoreStaff extends AuditableEntity
├── user    (@ManyToOne → User)
├── store   (@ManyToOne → Store)
├── storeRole   (StoreRole enum: OWNER, EMPLOYEE, DELIVERY)
├── status      (StoreStaffStatus enum: ACTIVE, INACTIVE)
└── unique constraint on (user_id, store_id)
```

### Why a Join Table

- A user can be OWNER of Store A and EMPLOYEE at Store B — a single enum on `User` can't express this
- Delivery staff may work for multiple stores
- Adding new roles (e.g., `MANAGER`) is just a new enum value — no schema change on `User`

### Enums

**`StoreRole`** — `com.example.fixit.module.store.entity.StoreRole`

| Value | Description |
|---|---|
| `OWNER` | Created the store. Full control over inventory, staff, orders, settings |
| `EMPLOYEE` | Manages inventory and processes orders for this specific store |
| `DELIVERY` | Handles deliveries. Can view and update delivery-related orders |

**`StoreStaffStatus`** — `com.example.fixit.module.store.entity.StoreStaffStatus`

| Value | Description |
|---|---|
| `ACTIVE` | Currently active staff member |
| `INACTIVE` | Deactivated (soft removal without deleting history) |

### Repository

**Location:** `com.example.fixit.module.store.repository.StoreStaffRepository`

```java
public interface StoreStaffRepository extends JpaRepository<StoreStaff, Long> {
    Optional<StoreStaff> findByUserIdAndStoreId(Long userId, Long storeId);
    List<StoreStaff> findByUserIdAndStatus(Long userId, StoreStaffStatus status);
    List<StoreStaff> findByStoreIdAndStatus(Long storeId, StoreStaffStatus status);
    boolean existsByUserIdAndStoreIdAndStoreRoleIn(Long userId, Long storeId, Collection<StoreRole> roles);
}
```

---

## Liquibase Migration: `V002__users_and_store_staff.sql`

```sql
-- users table
CREATE SEQUENCE IF NOT EXISTS users_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE users (
    id           BIGINT       NOT NULL DEFAULT nextval('users_seq'),
    keycloak_id  VARCHAR(255) NOT NULL,
    email        VARCHAR(150) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP,
    version      BIGINT,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_keycloak_id UNIQUE (keycloak_id)
);

CREATE INDEX idx_users_keycloak_id ON users(keycloak_id);

-- store_staff table
CREATE SEQUENCE IF NOT EXISTS store_staff_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE store_staff (
    id               BIGINT      NOT NULL DEFAULT nextval('store_staff_seq'),
    user_id          BIGINT      NOT NULL,
    store_id         BIGINT      NOT NULL,
    store_role       VARCHAR(20) NOT NULL,
    status           VARCHAR(20) NOT NULL,
    created_at       TIMESTAMP   NOT NULL,
    updated_at       TIMESTAMP,
    created_by       VARCHAR(255),
    last_modified_by VARCHAR(255),
    version          BIGINT,
    CONSTRAINT pk_store_staff PRIMARY KEY (id),
    CONSTRAINT fk_store_staff_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_store_staff_store FOREIGN KEY (store_id) REFERENCES stores(id),
    CONSTRAINT uk_store_staff_user_store UNIQUE (user_id, store_id)
);

CREATE INDEX idx_store_staff_user ON store_staff(user_id);
CREATE INDEX idx_store_staff_store ON store_staff(store_id);
```

---

## What's Intentionally Skipped

| Skipped | Reason |
|---|---|
| `UserService` with full CRUD | Not needed yet. The filter handles creation; cart service looks up users by `keycloakId` |
| User-facing endpoints | Keycloak handles registration, profile, password reset. The local `User` is internal |
| Role sync from Keycloak | Roles live in the JWT, read on every request. No need to persist them locally |
| `UserDTO` / mapper | No user endpoints = no DTOs needed yet |

---

## Authorization Flow (How It All Fits Together)

```
Incoming request with Bearer JWT
        │
        ▼
┌─────────────────────────────┐
│  OAuth2 Resource Server     │  ← validates JWT signature + expiry
│  Filter                     │
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│  UserProvisioningFilter     │  ← ensures User record exists
│  (OncePerRequestFilter)     │
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│  SecurityConfig             │  ← endpoint-level access (ROLE_BUYER, etc.)
│  @PreAuthorize              │
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│  Service Layer              │  ← business-level access (StoreStaff check)
│  e.g., "is this user OWNER  │
│  of this store?"            │
└─────────────────────────────┘
```
