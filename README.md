# FixIt — Database Schema Design

FixIt is an automotive parts and repair services marketplace. This repo contains the JPA entity model built with Spring Boot.

---

## Entity Hierarchy

All entities inherit from one of two abstract base classes:

```
BaseEntity
├── id (PK, auto-generated)
├── created_at
├── updated_at
└── version (optimistic locking)

AuditableEntity extends BaseEntity
├── created_by
└── last_modified_by
```

Both `Store` and `Product` extend `AuditableEntity`.

---

## Tables

### `stores`

Represents auto parts retailers and/or repair shops.

| Column | Type | Notes |
|---|---|---|
| id | BIGINT | PK |
| name | VARCHAR(100) | required |
| description | VARCHAR(500) | optional |
| phone | VARCHAR(20) | required |
| email | VARCHAR(100) | required |
| address | VARCHAR(255) | required |
| city | VARCHAR(100) | required |
| district | VARCHAR(100) | required |
| governorate | VARCHAR(100) | required |
| status | ENUM | ACTIVE, INACTIVE, SUSPENDED, CLOSED |
| service_type | ENUM | AUTOPARTS_ONLY, AUTOPARTS_AND_REPAIR |
| fulfillment_mode | ENUM | IN_SHOP, DELIVERY, HYBRID |
| rating | DECIMAL(2,1) | 0.0–5.0 |
| created_at, updated_at | TIMESTAMP | auto-managed |
| created_by, last_modified_by | VARCHAR | audit trail |
| version | BIGINT | optimistic locking |

---

### `products`

Base table for all product types using **joined-table inheritance**.

| Column | Type | Notes |
|---|---|---|
| id | BIGINT | PK |
| product_type | VARCHAR | discriminator (BRAKE, BATTERY, etc.) |
| name | VARCHAR(100) | required |
| price | DECIMAL(10,2) | required |
| stock | BIGINT | default 0 |
| brand | VARCHAR(50) | required |
| description | VARCHAR(1000) | optional |
| status | ENUM | ACTIVE, INACTIVE, OUT_OF_STOCK, DISCONTINUED |
| condition | ENUM | NEW, USED |
| store_id | BIGINT | FK → stores |
| created_at, updated_at | TIMESTAMP | auto-managed |
| created_by, last_modified_by | VARCHAR | audit trail |
| version | BIGINT | optimistic locking |

---

### `product_images`

| Column | Type | Notes |
|---|---|---|
| id | BIGINT | PK |
| product_id | BIGINT | FK → products (cascade delete) |
| url | VARCHAR(500) | public URL |
| s3_key | VARCHAR(300) | S3 object key for deletion |
| is_primary | BOOLEAN | default false |
| display_order | INT | ordering |
| file_name | VARCHAR(255) | |
| file_size_bytes | BIGINT | |
| content_type | ENUM | IMAGE_JPEG, IMAGE_PNG, IMAGE_WEBP |

Index on `(product_id, is_primary)` for fast primary image lookup.

---

## Product Subtypes (Joined Tables)

Each subtype has its own table joined to `products` on `id`.

### `brakes`
| Column | Notes |
|---|---|
| brake_type | DISC, DRUM |
| axle_position | FRONT, REAR |
| diameter_mm | |
| thickness_mm | |
| compatible_models | |

### `batteries`
| Column | Notes |
|---|---|
| capacity_ah | Ampere-hours |
| cold_cranking_amps | CCA |
| voltage | typically 12V |
| terminal_layout | e.g. TOP_LEFT |
| length_mm, width_mm, height_mm | dimensions |

### `air_filters`
| Column | Notes |
|---|---|
| filter_type | PANEL, ROUND, CONICAL |
| length_mm, width_mm, height_mm | dimensions |
| compatible_models | |

### `oil_filters`
| Column | Notes |
|---|---|
| thread_size | e.g. M20x1.5 |
| outer_diameter_mm | |
| height_mm | |
| bypass_valve_pressure_kpa | optional |
| compatible_models | |

### `spark_plugs`
| Column | Notes |
|---|---|
| plug_type | COPPER, PLATINUM, IRIDIUM, DOUBLE_PLATINUM |
| thread_diameter_mm | |
| thread_reach_mm | |
| gap_mm | |
| heat_range | |
| compatible_models | |

### `shock_absorbers`
| Column | Notes |
|---|---|
| absorber_type | GAS, OIL, ELECTRONIC |
| axle_position | FRONT, REAR |
| extended_length_mm | |
| compressed_length_mm | |
| compatible_models | |

### `suspension_springs`
| Column | Notes |
|---|---|
| axle_position | FRONT, REAR |
| spring_rate_n_per_mm | |
| free_length_mm | |
| coil_diameter_mm | |
| compatible_models | |

### `tires`
| Column | Notes |
|---|---|
| width_mm | |
| aspect_ratio | |
| rim_diameter_inches | |
| season | SUMMER, WINTER, ALL_SEASON, ALL_WEATHER |
| load_index | |
| speed_rating | |
| run_flat | boolean |
| fuel_efficiency_rating | EU label A–G |

### `alternators`
| Column | Notes |
|---|---|
| voltage | 12V or 24V |
| amperage | output in amps |
| pulley_type | FIXED, DECOUPLER, OVERRUNNING |
| compatible_models | |

### `transmission_fluids`
| Column | Notes |
|---|---|
| fluid_type | ATF, MTF, CVT, DCT |
| viscosity_grade | e.g. ATF+4, Dexron VI |
| volume_liters | |
| compatible_models | |

---

## Relationships

```
Store (1) ──────────── (*) Product
Product (1) ──────────── (*) ProductImage
```

- `Store → Products`: one-to-many, lazy-loaded, no orphan removal (products can exist without a store)
- `Product → ProductImages`: one-to-many, cascade ALL + orphan removal (images deleted with product)

---

## Design Patterns

- Joined-table inheritance — each product subtype has its own table, keeping `products` lean
- Optimistic locking via `@Version` — prevents lost updates under concurrent writes
- Spring Data Auditing — auto-populates timestamps and user fields
- Discriminator column `product_type` — enables polymorphic queries on the base table
