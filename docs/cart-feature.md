## Step 1: Keycloak Setup & Spring Security Integration

Foundation :

- Add `spring-boot-starter-oauth2-resource-server` to `build.gradle` (your API acts as a Resource Server, Keycloak is the Authorization Server)
- Configure Keycloak realm, client, and roles (`BUYER`, `SELLER`, `ADMIN`) in Keycloak itself
- Add `spring.security.oauth2.resourceserver.jwt.issuer-uri` pointing to your Keycloak realm
- Create a `SecurityConfig` class with `SecurityFilterChain` that:
  - Permits public endpoints (catalog browsing)
  - Requires authentication for cart/order endpoints
  - Maps Keycloak realm roles to Spring Security authorities
- Wire the JWT `sub` claim into Spring's `AuditorAware` so `createdBy`/`lastModifiedBy` on your `AuditableEntity` gets populated automatically

This approach keeps your backend stateless (JWT validation only, no session) which is the standard for REST APIs with Keycloak.

### Decisions

- **Frontend**: SPA (React or similar) with a `public` Keycloak client using Authorization Code + PKCE flow. The backend is a pure REST API — it never handles the login flow, only validates Bearer JWTs.
- **Local Dev**: Keycloak container added to `docker-compose.yml` alongside Postgres, running on port 8180.
- **Seller–Store Relationship**: Managed via a `StoreStaff` join table with roles (`OWNER`, `EMPLOYEE`, `DELIVERY`) rather than a direct `@ManyToOne User owner` on Store. This supports multiple staff per store and users working at multiple stores. See [user-roles.md](user-roles.md) for full design.

## Step 2: User Entity & Role Model

> Role design: [user-roles.md](user-roles.md) · Implementation approach: [user-provisioning.md](user-provisioning.md)

A `UserProvisioningFilter` (`OncePerRequestFilter`) auto-creates the local `User` record on the first authenticated request using JWT claims (`sub`, `email`, `preferred_username`). This runs after JWT validation in the filter chain, so the `User` row exists before any controller, `@PreAuthorize`, or `AuditorAware` needs it. The `StoreStaff` join table maps users to stores with granular roles (`OWNER`, `EMPLOYEE`, `DELIVERY`). See [user-provisioning.md](user-provisioning.md) for the full implementation details including filter design, race-condition handling, service layer, and Liquibase migration.

## Step 3: Cart Domain Model

Two entities following your existing patterns:

- `Cart` extends `AuditableEntity`:
  - `@OneToOne` to `User` (one active cart per user)
  - `@OneToMany` to `CartItem` (cascade ALL, orphanRemoval)
  - `status` enum: `ACTIVE`, `CHECKED_OUT`, `ABANDONED`
  
- `CartItem` extends `BaseEntity`:
  - `@ManyToOne` to `Cart`
  - `@ManyToOne` to `Product`
  - `quantity` (Integer, validated > 0)
  - `unitPrice` (BigDecimal — snapshot of price at time of add, protects against price changes)

This is the standard e-commerce pattern: price snapshot at cart-add time, validated against current price at checkout.

## Step 4: Cart Module Structure

Following your existing module conventions:

```
module/cart/
├── controller/CartController.java
├── dto/
│   ├── AddToCartRequestDTO.java
│   ├── UpdateCartItemRequestDTO.java
│   └── CartResponseDTO.java
├── entity/
│   ├── Cart.java
│   ├── CartItem.java
│   └── CartStatus.java
├── mapper/CartMapper.java
├── repository/CartRepository.java
├── service/
│   ├── CartService.java
│   └── CartServiceImpl.java
└── exception/
    └── CartNotFoundException.java (+ others as needed)
```

## Step 5: Cart API Endpoints

```
POST   /api/cart/items              — Add product to cart (productId, quantity)
GET    /api/cart                    — Get current user's active cart
PUT    /api/cart/items/{itemId}     — Update quantity
DELETE /api/cart/items/{itemId}     — Remove item
DELETE /api/cart                    — Clear cart
```

All endpoints extract the user from the JWT SecurityContext — no userId in the URL (prevents IDOR vulnerabilities).

## Step 6: Business Logic Considerations

- Stock validation: check `product.stock >= requestedQuantity` on add/update
- Multi-store cart: decide if a cart can hold items from different stores (most marketplaces allow this)
- Optimistic locking: your `BaseEntity` already has `@Version`, so concurrent cart modifications are handled
- Idempotency: if the user adds the same product twice, increment quantity rather than creating a duplicate `CartItem`
- Price drift: store `unitPrice` on the `CartItem`, and optionally warn the user at checkout if the product price has changed

## Implementation Order

I'd recommend this sequence:

1. Keycloak + Spring Security config (unblocks everything)
2. User entity + auto-provisioning
3. Cart/CartItem entities + repository
4. CartService + CartMapper
5. CartController
6. Exception handling (extend your existing `GlobalExceptionHandler`)

Want me to start implementing? I can begin with any step, though Step 1 (Keycloak/Security) is the natural starting point since the cart endpoints need authentication. Let me know how you'd like to proceed.