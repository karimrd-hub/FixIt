# FixIt — Auto Parts Platform

FixIt is a Spring Boot backend for an auto parts e-commerce platform. The focus of this project is clean, extensible architecture and a well-designed catalog search feature.

---

## Architecture & Design Principles

- Modular package structure — each domain (catalog, product, store, cart) lives in its own module under `com.example.fixit.module`
- Mappers handle all entity-to-DTO conversion; no mapping logic leaks into controllers or services
- `@Transactional(readOnly = true)` on read-only services for consistent snapshots and Hibernate performance
- `JpaConfig` is a dedicated configuration class (not on the main app class) to keep JPA auditing and `AuditorAware` concerns separated
- Lombok for boilerplate reduction, SLF4J for logging
- All list endpoints are paginated

## Catalog and Product Browsing Feature

The catalog exposes two main endpoints under `/api/catalog`:

**GET /api/catalog/categories** — returns all available product categories.

**GET /api/catalog/products/filter** — filters products by any combination of optional query params:
`categorySlug`, `governorate`, `district`, `city`, `condition`, `status`, `fulfillmentMode`, `serviceType`, `brand`, `priceMin`, `priceMax`, `searchTerm`

### How it works

- `ProductCategory` enum maps each category slug to its concrete entity subtype (e.g. `TIRE → Tire.class`). Adding a new category is a single enum entry — no other code changes needed.
- `ProductRepository` uses `TYPE(p)` in JPQL to query by concrete subtype without needing per-subtype repositories.
- `ProductSpecifications` provides composable, null-safe JPA Specifications — one per filter criterion. Only the filters actually provided get applied.
- `CatalogServiceImpl` applies a local-first sourcing strategy:
  1. Query products from stores in the customer's city → `source: "local"`
  2. If empty, fall back to stores in other cities → `source: "remote"`
  3. If still empty, return an empty result → `source: "none"`
- Store results are sorted by `rating DESC` at the query level.

## Tech Stack

- Java 21, Spring Boot 4.0.2
- Spring Data JPA + Hibernate (single-table inheritance for product subtypes)
- JPA Specifications for dynamic filtering
- Gradle
