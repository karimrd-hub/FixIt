--liquibase formatted sql

--changeset fixit:003-create-carts
CREATE SEQUENCE IF NOT EXISTS carts_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE carts (
    id               BIGINT      NOT NULL DEFAULT nextval('carts_seq'),
    user_id          BIGINT      NOT NULL,
    status           VARCHAR(20) NOT NULL,
    created_at       TIMESTAMP   NOT NULL,
    updated_at       TIMESTAMP,
    created_by       VARCHAR(255),
    last_modified_by VARCHAR(255),
    version          BIGINT,
    CONSTRAINT pk_carts PRIMARY KEY (id),
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_carts_user UNIQUE (user_id)
);

CREATE INDEX idx_carts_user ON carts(user_id);
CREATE INDEX idx_carts_status ON carts(status);

--changeset fixit:003-create-cart-items
CREATE SEQUENCE IF NOT EXISTS cart_items_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE cart_items (
    id         BIGINT        NOT NULL DEFAULT nextval('cart_items_seq'),
    cart_id    BIGINT        NOT NULL,
    product_id BIGINT        NOT NULL,
    quantity   INTEGER       NOT NULL,
    unit_price NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP     NOT NULL,
    updated_at TIMESTAMP,
    version    BIGINT,
    CONSTRAINT pk_cart_items PRIMARY KEY (id),
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT uk_cart_items_cart_product UNIQUE (cart_id, product_id),
    CONSTRAINT ck_cart_items_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product ON cart_items(product_id);
