# Entity Relationship Diagram

```mermaid
erDiagram
    stores {
        bigint id PK
        varchar name
        varchar description
        varchar phone
        varchar email
        varchar address
        varchar city
        varchar status
        decimal rating
        timestamp created_at
        timestamp updated_at
        varchar created_by
        varchar last_modified_by
        bigint version
    }

    products {
        bigint id PK
        varchar product_type
        varchar name
        decimal price
        bigint stock
        varchar brand
        varchar description
        varchar status
        bigint store_id FK
        timestamp created_at
        timestamp updated_at
        varchar created_by
        varchar last_modified_by
        bigint version
    }

    tires {
        bigint id PK "FK -> products"
        int width_mm
        int aspect_ratio
        int rim_diameter_inches
        varchar season
        int load_index
        varchar speed_rating
        boolean run_flat
        varchar fuel_efficiency_rating
    }

    brakes {
        bigint id PK "FK -> products"
        varchar brake_type
        varchar axle_position
        int diameter_mm
        int thickness_mm
        varchar compatible_models
    }

    batteries {
        bigint id PK "FK -> products"
        int capacity_ah
        int cold_cranking_amps
        int voltage
        varchar terminal_layout
        int length_mm
        int width_mm
        int height_mm
    }

    oil_filters {
        bigint id PK "FK -> products"
        varchar thread_size
        int outer_diameter_mm
        int height_mm
        int bypass_valve_pressure_kpa
        varchar compatible_models
    }

    shock_absorbers {
        bigint id PK "FK -> products"
        varchar absorber_type
        varchar axle_position
        int extended_length_mm
        int compressed_length_mm
        varchar compatible_models
    }

    product_images {
        bigint id PK
        bigint product_id FK
        varchar url
        varchar s3_key
        boolean is_primary
        int display_order
        varchar file_name
        bigint file_size_bytes
        varchar content_type
        timestamp created_at
        timestamp updated_at
        bigint version
    }

    stores ||--o{ products : "has"
    products ||--o{ product_images : "has"
    products ||--o| tires : "is"
    products ||--o| brakes : "is"
    products ||--o| batteries : "is"
    products ||--o| oil_filters : "is"
    products ||--o| shock_absorbers : "is"
```

## Inheritance Strategy

`products` uses **JOINED** table inheritance with a `product_type` discriminator column.
Each subtype table shares the same PK as `products` and only stores its own specific columns.

## Enums

| Entity | Column | Values |
|---|---|---|
| products | status | ACTIVE, INACTIVE, OUT_OF_STOCK, DISCONTINUED |
| stores | status | ACTIVE, INACTIVE, SUSPENDED, CLOSED |
| tires | season | SUMMER, WINTER, ALL_SEASON, ALL_WEATHER |
| brakes | brake_type | DISC, DRUM |
| shock_absorbers | absorber_type | GAS, OIL, ELECTRONIC |
| product_images | content_type | IMAGE_JPEG, IMAGE_PNG, IMAGE_WEBP |
