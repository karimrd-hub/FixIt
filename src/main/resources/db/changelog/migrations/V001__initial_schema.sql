--liquibase formatted sql

--changeset fixit:001-create-sequences
CREATE SEQUENCE IF NOT EXISTS stores_seq       START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS products_seq     START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS product_images_seq START WITH 1 INCREMENT BY 50;

--changeset fixit:001-create-stores
CREATE TABLE stores (
    id                 BIGINT       NOT NULL DEFAULT nextval('stores_seq'),
    name               VARCHAR(100) NOT NULL,
    description        VARCHAR(500),
    phone              VARCHAR(20)  NOT NULL,
    email              VARCHAR(100) NOT NULL,
    address            VARCHAR(255) NOT NULL,
    city               VARCHAR(100) NOT NULL,
    district           VARCHAR(100) NOT NULL,
    governorate        VARCHAR(100) NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    service_type       VARCHAR(25)  NOT NULL,
    fulfillment_mode   VARCHAR(20)  NOT NULL,
    rating             NUMERIC(2,1) NOT NULL DEFAULT 0.0,
    created_at         TIMESTAMP    NOT NULL,
    updated_at         TIMESTAMP,
    created_by         VARCHAR(255),
    last_modified_by   VARCHAR(255),
    version            BIGINT,
    CONSTRAINT pk_stores PRIMARY KEY (id)
);

--changeset fixit:001-create-products
CREATE TABLE products (
    id                 BIGINT        NOT NULL DEFAULT nextval('products_seq'),
    product_type       VARCHAR(31)   NOT NULL,
    name               VARCHAR(100)  NOT NULL,
    price              NUMERIC(10,2) NOT NULL,
    stock              BIGINT        NOT NULL DEFAULT 0,
    brand              VARCHAR(50)   NOT NULL,
    description        VARCHAR(1000),
    status             VARCHAR(20)   NOT NULL,
    product_condition  VARCHAR(10)   NOT NULL,
    store_id           BIGINT,
    created_at         TIMESTAMP     NOT NULL,
    updated_at         TIMESTAMP,
    created_by         VARCHAR(255),
    last_modified_by   VARCHAR(255),
    version            BIGINT,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT fk_products_store FOREIGN KEY (store_id) REFERENCES stores(id)
);

--changeset fixit:001-create-product-images
CREATE TABLE product_images (
    id              BIGINT       NOT NULL DEFAULT nextval('product_images_seq'),
    product_id      BIGINT       NOT NULL,
    url             VARCHAR(500) NOT NULL,
    s3_key          VARCHAR(300) NOT NULL,
    is_primary      BOOLEAN      NOT NULL DEFAULT FALSE,
    display_order   INTEGER      NOT NULL DEFAULT 0,
    file_name       VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT       NOT NULL,
    content_type    VARCHAR(20)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP,
    version         BIGINT,
    CONSTRAINT pk_product_images PRIMARY KEY (id),
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_product_images_primary ON product_images(product_id, is_primary);

--changeset fixit:001-create-tires
CREATE TABLE tires (
    id                     BIGINT      NOT NULL,
    width_mm               INTEGER     NOT NULL,
    aspect_ratio           INTEGER     NOT NULL,
    rim_diameter_inches    INTEGER     NOT NULL,
    season                 VARCHAR(20) NOT NULL,
    load_index             INTEGER     NOT NULL,
    speed_rating           VARCHAR(5)  NOT NULL,
    run_flat               BOOLEAN     NOT NULL DEFAULT FALSE,
    fuel_efficiency_rating VARCHAR(2),
    CONSTRAINT pk_tires PRIMARY KEY (id),
    CONSTRAINT fk_tires_product FOREIGN KEY (id) REFERENCES products(id)
);

--changeset fixit:001-create-brakes
CREATE TABLE brakes (
    id                BIGINT       NOT NULL,
    brake_type        VARCHAR(20)  NOT NULL,
    axle_position     VARCHAR(10)  NOT NULL,
    diameter_mm       INTEGER      NOT NULL,
    thickness_mm      INTEGER      NOT NULL,
    compatible_models VARCHAR(500),
    CONSTRAINT pk_brakes PRIMARY KEY (id),
    CONSTRAINT fk_brakes_product FOREIGN KEY (id) REFERENCES products(id)
);

--changeset fixit:001-create-batteries
CREATE TABLE batteries (
    id                 BIGINT      NOT NULL,
    capacity_ah        INTEGER     NOT NULL,
    cold_cranking_amps INTEGER     NOT NULL,
    voltage            INTEGER     NOT NULL,
    terminal_layout    VARCHAR(20) NOT NULL,
    length_mm          INTEGER     NOT NULL,
    width_mm           INTEGER     NOT NULL,
    height_mm          INTEGER     NOT NULL,
    CONSTRAINT pk_batteries PRIMARY KEY (id),
    CONSTRAINT fk_batteries_product FOREIGN KEY (id) REFERENCES products(id)
);

--changeset fixit:001-create-alternators
CREATE TABLE alternators (
    id                BIGINT       NOT NULL,
    voltage           INTEGER      NOT NULL,
    amperage          INTEGER      NOT NULL,
    pulley_type       VARCHAR(20)  NOT NULL,
    compatible_models VARCHAR(500),
    CONSTRAINT pk_alternators PRIMARY KEY (id),
    CONSTRAINT fk_alternators_product FOREIGN KEY (id) REFERENCES products(id)
);

--changeset fixit:001-create-air-filters
CREATE TABLE air_filters (
    id                BIGINT       NOT NULL,
    filter_type       VARCHAR(20)  NOT NULL,
    length_mm         INTEGER      NOT NULL,
    width_mm          INTEGER      NOT NULL,
    height_mm         INTEGER      NOT NULL,
    compatible_models VARCHAR(500),
    CONSTRAINT pk_air_filters PRIMARY KEY (id),
    CONSTRAINT fk_air_filters_product FOREIGN KEY (id) REFERENCES products(id)
);

--changeset fixit:001-create-oil-filters
CREATE TABLE oil_filters (
    id                       BIGINT       NOT NULL,
    thread_size              VARCHAR(20)  NOT NULL,
    outer_diameter_mm        INTEGER      NOT NULL,
    height_mm                INTEGER      NOT NULL,
    bypass_valve_pressure_kpa INTEGER,
    compatible_models        VARCHAR(500),
    CONSTRAINT pk_oil_filters PRIMARY KEY (id),
    CONSTRAINT fk_oil_filters_product FOREIGN KEY (id) REFERENCES products(id)
);

--changeset fixit:001-create-shock-absorbers
CREATE TABLE shock_absorbers (
    id                  BIGINT       NOT NULL,
    absorber_type       VARCHAR(20)  NOT NULL,
    axle_position       VARCHAR(10)  NOT NULL,
    extended_length_mm  INTEGER      NOT NULL,
    compressed_length_mm INTEGER     NOT NULL,
    compatible_models   VARCHAR(500),
    CONSTRAINT pk_shock_absorbers PRIMARY KEY (id),
    CONSTRAINT fk_shock_absorbers_product FOREIGN KEY (id) REFERENCES products(id)
);

--changeset fixit:001-create-spark-plugs
CREATE TABLE spark_plugs (
    id                 BIGINT      NOT NULL,
    plug_type          VARCHAR(20) NOT NULL,
    thread_diameter_mm INTEGER     NOT NULL,
    thread_reach_mm    INTEGER     NOT NULL,
    gap_mm             DOUBLE PRECISION NOT NULL,
    heat_range         INTEGER     NOT NULL,
    compatible_models  VARCHAR(500),
    CONSTRAINT pk_spark_plugs PRIMARY KEY (id),
    CONSTRAINT fk_spark_plugs_product FOREIGN KEY (id) REFERENCES products(id)
);

--changeset fixit:001-create-suspension-springs
CREATE TABLE suspension_springs (
    id                   BIGINT       NOT NULL,
    axle_position        VARCHAR(10)  NOT NULL,
    spring_rate_n_per_mm INTEGER      NOT NULL,
    free_length_mm       INTEGER      NOT NULL,
    coil_diameter_mm     INTEGER      NOT NULL,
    compatible_models    VARCHAR(500),
    CONSTRAINT pk_suspension_springs PRIMARY KEY (id),
    CONSTRAINT fk_suspension_springs_product FOREIGN KEY (id) REFERENCES products(id)
);

--changeset fixit:001-create-transmission-fluids
CREATE TABLE transmission_fluids (
    id                BIGINT           NOT NULL,
    fluid_type        VARCHAR(20)      NOT NULL,
    viscosity_grade   VARCHAR(20)      NOT NULL,
    volume_liters     DOUBLE PRECISION NOT NULL,
    compatible_models VARCHAR(500),
    CONSTRAINT pk_transmission_fluids PRIMARY KEY (id),
    CONSTRAINT fk_transmission_fluids_product FOREIGN KEY (id) REFERENCES products(id)
);
