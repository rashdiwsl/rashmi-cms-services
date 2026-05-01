-- ===========================================
-- CMS DATABASE SCHEMA
-- ===========================================

CREATE DATABASE IF NOT EXISTS cms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cms_db;

-- -------------------------------------------
-- MASTER DATA: Country
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS country (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE,
    code    VARCHAR(10)  NOT NULL UNIQUE
);

-- -------------------------------------------
-- MASTER DATA: City
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS city (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    country_id  BIGINT NOT NULL,
    CONSTRAINT fk_city_country FOREIGN KEY (country_id) REFERENCES country(id)
);

-- -------------------------------------------
-- CUSTOMER
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS customer (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    date_of_birth DATE         NOT NULL,
    nic_number    VARCHAR(20)  NOT NULL UNIQUE,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- -------------------------------------------
-- MOBILE NUMBERS (multiple per customer)
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS customer_mobile (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id   BIGINT       NOT NULL,
    mobile_number VARCHAR(20)  NOT NULL,
    CONSTRAINT fk_mobile_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE
);

-- -------------------------------------------
-- ADDRESSES (multiple per customer)
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS customer_address (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id   BIGINT       NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city_id       BIGINT       NOT NULL,
    country_id    BIGINT       NOT NULL,
    CONSTRAINT fk_address_customer FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT fk_address_city     FOREIGN KEY (city_id)     REFERENCES city(id),
    CONSTRAINT fk_address_country  FOREIGN KEY (country_id)  REFERENCES country(id)
);

-- -------------------------------------------
-- FAMILY MEMBERS (customer linked to customer)
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS customer_family (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id       BIGINT NOT NULL,
    family_member_id  BIGINT NOT NULL,
    CONSTRAINT fk_family_customer FOREIGN KEY (customer_id)      REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT fk_family_member   FOREIGN KEY (family_member_id) REFERENCES customer(id) ON DELETE CASCADE,
    CONSTRAINT uq_family          UNIQUE (customer_id, family_member_id)
);