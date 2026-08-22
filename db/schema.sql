-- Mini D-Mart schema
-- Target: MySQL 8.0

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS returns;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS pickup_slots;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS carts;
DROP TABLE IF EXISTS stock_movements;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS addresses;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- Roles & Users
-- ---------------------------------------------------------------------
CREATE TABLE roles (
    role_id     INT AUTO_INCREMENT PRIMARY KEY,
    role_name   VARCHAR(20) NOT NULL UNIQUE   -- CUSTOMER, STAFF, ADMIN
);

CREATE TABLE users (
    user_id         INT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    phone           VARCHAR(20),
    role_id         INT NOT NULL,
    is_active       TINYINT(1) NOT NULL DEFAULT 1,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
) ;

CREATE TABLE addresses (
    address_id      INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    label           VARCHAR(50) DEFAULT 'Home',
    line1           VARCHAR(200) NOT NULL,
    line2           VARCHAR(200),
    city            VARCHAR(100) NOT NULL,
    state           VARCHAR(100) NOT NULL,
    pincode         VARCHAR(15) NOT NULL,
    is_default      TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_addr_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Catalog
-- ---------------------------------------------------------------------
CREATE TABLE categories (
    category_id     INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    description     VARCHAR(255)
);

CREATE TABLE products (
    product_id      INT AUTO_INCREMENT PRIMARY KEY,
    category_id     INT NOT NULL,
    sku             VARCHAR(40) NOT NULL UNIQUE,
    name            VARCHAR(150) NOT NULL,
    description     VARCHAR(500),
    unit            VARCHAR(30) NOT NULL DEFAULT 'each',   -- e.g. 1kg, 500ml, each
    price           DECIMAL(10,2) NOT NULL,
    image_url       VARCHAR(300),
    stock_qty       INT NOT NULL DEFAULT 0,
    reorder_level   INT NOT NULL DEFAULT 10,
    is_active       TINYINT(1) NOT NULL DEFAULT 1,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

CREATE TABLE stock_movements (
    movement_id     INT AUTO_INCREMENT PRIMARY KEY,
    product_id      INT NOT NULL,
    change_qty      INT NOT NULL,             -- positive = stock in, negative = stock out
    reason          VARCHAR(50) NOT NULL,      -- ORDER_PLACED, ORDER_CANCELLED, RETURN_RESTOCK, MANUAL_ADJUST, EXCHANGE_OUT, EXCHANGE_IN
    reference_type  VARCHAR(30),
    reference_id    INT,
    created_by      INT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stockmv_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT fk_stockmv_user FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- ---------------------------------------------------------------------
-- Cart
-- ---------------------------------------------------------------------
CREATE TABLE carts (
    cart_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL UNIQUE,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE cart_items (
    cart_item_id    INT AUTO_INCREMENT PRIMARY KEY,
    cart_id         INT NOT NULL,
    product_id      INT NOT NULL,
    quantity        INT NOT NULL,
    added_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cart_product (cart_id, product_id),
    CONSTRAINT fk_cartitem_cart FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    CONSTRAINT fk_cartitem_product FOREIGN KEY (product_id) REFERENCES products(product_id),
    CONSTRAINT chk_cartitem_qty CHECK (quantity > 0)
);

-- ---------------------------------------------------------------------
-- Pickup slots (capacity-managed scheduled store pickup)
-- ---------------------------------------------------------------------
CREATE TABLE pickup_slots (
    slot_id         INT AUTO_INCREMENT PRIMARY KEY,
    slot_date       DATE NOT NULL,
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    capacity        INT NOT NULL DEFAULT 10,
    booked_count    INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_slot (slot_date, start_time)
);

-- ---------------------------------------------------------------------
-- Orders
-- ---------------------------------------------------------------------
CREATE TABLE orders (
    order_id            INT AUTO_INCREMENT PRIMARY KEY,
    order_number        VARCHAR(30) NOT NULL UNIQUE,
    user_id             INT NOT NULL,
    fulfillment_type    ENUM('PICKUP','DELIVERY') NOT NULL,
    status              ENUM('PLACED','CONFIRMED','PREPARING','READY_FOR_PICKUP',
                              'OUT_FOR_DELIVERY','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PLACED',
    subtotal            DECIMAL(10,2) NOT NULL,
    delivery_fee        DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_amount        DECIMAL(10,2) NOT NULL,
    pickup_slot_id      INT,
    delivery_address_id INT,
    placed_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    cancelled_at        TIMESTAMP NULL,
    cancel_reason       VARCHAR(255),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_order_slot FOREIGN KEY (pickup_slot_id) REFERENCES pickup_slots(slot_id),
    CONSTRAINT fk_order_addr FOREIGN KEY (delivery_address_id) REFERENCES addresses(address_id)
);

CREATE TABLE order_items (
    order_item_id       INT AUTO_INCREMENT PRIMARY KEY,
    order_id            INT NOT NULL,
    product_id          INT NOT NULL,
    product_name_snap   VARCHAR(150) NOT NULL,
    unit_price_snap     DECIMAL(10,2) NOT NULL,
    quantity            INT NOT NULL,
    line_total           DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_orderitem_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_orderitem_product FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- ---------------------------------------------------------------------
-- Returns / Exchanges
-- ---------------------------------------------------------------------
CREATE TABLE returns (
    return_id           INT AUTO_INCREMENT PRIMARY KEY,
    order_item_id        INT NOT NULL,
    user_id              INT NOT NULL,
    type                 ENUM('RETURN','EXCHANGE') NOT NULL,
    reason               VARCHAR(255) NOT NULL,
    quantity             INT NOT NULL,
    status               ENUM('REQUESTED','APPROVED','REJECTED','COMPLETED') NOT NULL DEFAULT 'REQUESTED',
    exchange_product_id  INT,
    staff_notes          VARCHAR(255),
    resolved_by          INT,
    requested_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at          TIMESTAMP NULL,
    CONSTRAINT fk_return_orderitem FOREIGN KEY (order_item_id) REFERENCES order_items(order_item_id),
    CONSTRAINT fk_return_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_return_exproduct FOREIGN KEY (exchange_product_id) REFERENCES products(product_id),
    CONSTRAINT fk_return_resolver FOREIGN KEY (resolved_by) REFERENCES users(user_id)
);

-- ---------------------------------------------------------------------
-- Audit log (security-relevant + business events)
-- ---------------------------------------------------------------------
CREATE TABLE audit_log (
    audit_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT,
    action          VARCHAR(60) NOT NULL,     -- LOGIN_SUCCESS, LOGIN_FAILURE, REGISTER, ORDER_PLACED, ORDER_CANCELLED, RETURN_APPROVED, ...
    entity_type     VARCHAR(40),
    entity_id       INT,
    details          VARCHAR(500),
    ip_address       VARCHAR(45),
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ---------------------------------------------------------------------
-- Indexes for common lookups
-- ---------------------------------------------------------------------
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_active ON products(is_active);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_returns_status ON returns(status);
CREATE INDEX idx_audit_user ON audit_log(user_id);

-- ---------------------------------------------------------------------
-- Reference data
-- ---------------------------------------------------------------------
INSERT INTO roles (role_name) VALUES ('CUSTOMER'), ('STAFF'), ('ADMIN');
