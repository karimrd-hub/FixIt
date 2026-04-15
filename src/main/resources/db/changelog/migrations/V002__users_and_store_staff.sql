--liquibase formatted sql

--changeset fixit:002-create-users
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

--changeset fixit:002-create-store-staff
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
