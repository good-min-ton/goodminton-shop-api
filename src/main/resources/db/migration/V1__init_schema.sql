-- ============================================================
-- V1__init_schema.sql
-- Badminton Store System — Initial Schema
-- ============================================================
-- ------------------------------------------------------------
-- ENUMS
-- ------------------------------------------------------------
CREATE TYPE user_role AS ENUM ('SUPER_ADMIN', 'STORE_ADMIN', 'CUSTOMER');

CREATE TYPE account_status AS ENUM ('ACTIVE', 'INACTIVE');

CREATE TYPE order_status AS ENUM (
    'PENDING',
    'CONFIRMED',
    'PREPARING',
    'SHIPPING',
    'DELIVERED',
    'COMPLETED',
    'CANCELLED',
    'RETURN_REQUESTED'
);

CREATE TYPE order_type AS ENUM ('ONLINE', 'IN_STORE');

CREATE TYPE payment_method AS ENUM ('COD', 'BANKING', 'VNPAY');

CREATE TYPE payment_status AS ENUM ('PENDING', 'PAID', 'FAILED');

CREATE TYPE size_type AS ENUM ('RACKET', 'NON_RACKET');

-- ------------------------------------------------------------
-- TABLES
-- ------------------------------------------------------------
CREATE TABLE
    accounts (
        id SERIAL PRIMARY KEY,
        full_name VARCHAR(50) NOT NULL,
        phone VARCHAR(20) NOT NULL UNIQUE,
        email VARCHAR(50) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        role user_role NOT NULL,
        status account_status NOT NULL DEFAULT 'ACTIVE',
        created_at TIMESTAMP NOT NULL DEFAULT NOW (),
        updated_at TIMESTAMP NOT NULL DEFAULT NOW ()
    );

-- ------------------------------------------------------------
CREATE TABLE
    stores (
        id SERIAL PRIMARY KEY,
        admin_id INTEGER NOT NULL,
        name VARCHAR(100) NOT NULL,
        address VARCHAR(255) NOT NULL,
        contact VARCHAR(20) NOT NULL,
        longitude DECIMAL(10, 7) NOT NULL,
        latitude DECIMAL(10, 7) NOT NULL,
        is_central BOOLEAN NOT NULL DEFAULT FALSE,
        created_at TIMESTAMP NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_stores_admin FOREIGN KEY (admin_id) REFERENCES accounts (id) ON DELETE RESTRICT
    );

-- ------------------------------------------------------------
CREATE TABLE
    categories (
        id SERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        description TEXT
    );

-- ------------------------------------------------------------
CREATE TABLE
    brands (id SERIAL PRIMARY KEY, name VARCHAR(100) NOT NULL);

-- ------------------------------------------------------------
CREATE TABLE
    products (
        id SERIAL PRIMARY KEY,
        category_id INTEGER NOT NULL,
        brand_id INTEGER NOT NULL,
        related_product_id INTEGER,
        name VARCHAR(100) NOT NULL,
        description TEXT,
        thumbnail_url VARCHAR(255) NOT NULL,
        slug VARCHAR(200) NOT NULL UNIQUE,
        is_visible BOOLEAN NOT NULL DEFAULT TRUE,
        created_at TIMESTAMP NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE RESTRICT,
        CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES brands (id) ON DELETE RESTRICT,
        CONSTRAINT fk_products_related FOREIGN KEY (related_product_id) REFERENCES products (id) ON DELETE SET NULL
    );

-- ------------------------------------------------------------
CREATE TABLE
    product_specifications (
        id SERIAL PRIMARY KEY,
        product_id INTEGER NOT NULL,
        name VARCHAR(50) NOT NULL,
        value VARCHAR(255) NOT NULL,
        CONSTRAINT fk_specs_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
    );

-- ------------------------------------------------------------
CREATE TABLE
    colors (
        id SERIAL PRIMARY KEY,
        name VARCHAR(50) NOT NULL UNIQUE
    );

-- ------------------------------------------------------------
CREATE TABLE
    sizes (
        id SERIAL PRIMARY KEY,
        name VARCHAR(50) NOT NULL UNIQUE,
        type size_type NOT NULL
    );

-- ------------------------------------------------------------
CREATE TABLE
    product_variants (
        id SERIAL PRIMARY KEY,
        product_id INTEGER NOT NULL,
        color_id INTEGER,
        size_id INTEGER,
        sku_code VARCHAR(100) NOT NULL UNIQUE,
        price DECIMAL(10, 2) NOT NULL,
        updated_at TIMESTAMP NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_variants_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
        CONSTRAINT fk_variants_color FOREIGN KEY (color_id) REFERENCES colors (id) ON DELETE RESTRICT,
        CONSTRAINT fk_variants_size FOREIGN KEY (size_id) REFERENCES sizes (id) ON DELETE RESTRICT,
        CONSTRAINT uq_variant UNIQUE (product_id, color_id, size_id)
    );

-- ------------------------------------------------------------
CREATE TABLE
    variant_images (
        id SERIAL PRIMARY KEY,
        variant_id INTEGER NOT NULL,
        public_id VARCHAR(255) NOT NULL,
        image_url VARCHAR(255) NOT NULL,
        sort_order INTEGER NOT NULL DEFAULT 0,
        created_at TIMESTAMP NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_images_variant FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE CASCADE
    );

-- ------------------------------------------------------------
CREATE TABLE
    product_discounts (
        id SERIAL PRIMARY KEY,
        variant_id INTEGER NOT NULL,
        sale_price DECIMAL(10, 2) NOT NULL,
        is_active BOOLEAN NOT NULL DEFAULT TRUE,
        start_time TIMESTAMP NOT NULL,
        end_time TIMESTAMP NOT NULL,
        created_at TIMESTAMP NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_discounts_variant FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE CASCADE,
        CONSTRAINT chk_discount_price CHECK (sale_price > 0),
        CONSTRAINT chk_discount_time CHECK (end_time > start_time)
    );

-- ------------------------------------------------------------
CREATE TABLE
    inventory (
        id SERIAL PRIMARY KEY,
        store_id INTEGER NOT NULL,
        variant_id INTEGER NOT NULL,
        quantity INTEGER NOT NULL DEFAULT 0,
        updated_at TIMESTAMP NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_inventory_store FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE RESTRICT,
        CONSTRAINT fk_inventory_variant FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE RESTRICT,
        CONSTRAINT uq_store_variant UNIQUE (store_id, variant_id),
        CONSTRAINT chk_inventory_quantity CHECK (quantity >= 0)
    );

-- ------------------------------------------------------------
CREATE TABLE
    orders (
        id SERIAL PRIMARY KEY,
        customer_id INTEGER NOT NULL,
        store_id INTEGER,
        order_type order_type NOT NULL,
        status order_status NOT NULL DEFAULT 'PENDING',
        total_amount DECIMAL(10, 2) NOT NULL,
        shipping_code VARCHAR(100),
        -- Thông tin người nhận (chỉ dùng cho ONLINE)
        recipient_name VARCHAR(100),
        recipient_phone VARCHAR(20),
        recipient_address VARCHAR(255),
        recipient_email VARCHAR(50),
        note TEXT,
        order_date TIMESTAMP NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES accounts (id) ON DELETE RESTRICT,
        CONSTRAINT fk_orders_store FOREIGN KEY (store_id) REFERENCES stores (id) ON DELETE RESTRICT,
        CONSTRAINT chk_orders_total CHECK (total_amount > 0)
    );

-- ------------------------------------------------------------
CREATE TABLE
    order_items (
        id SERIAL PRIMARY KEY,
        order_id INTEGER NOT NULL,
        variant_id INTEGER NOT NULL,
        quantity INTEGER NOT NULL,
        unit_price DECIMAL(10, 2) NOT NULL,
        discount_price DECIMAL(10, 2),
        CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
        CONSTRAINT fk_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants (id) ON DELETE RESTRICT,
        CONSTRAINT chk_items_quantity CHECK (quantity > 0),
        CONSTRAINT chk_items_unit_price CHECK (unit_price > 0)
    );

-- ------------------------------------------------------------
CREATE TABLE
    order_item_allocations (
        id SERIAL PRIMARY KEY,
        order_item_id INTEGER NOT NULL,
        inventory_id INTEGER NOT NULL,
        quantity INTEGER NOT NULL,
        CONSTRAINT fk_allocations_item FOREIGN KEY (order_item_id) REFERENCES order_items (id) ON DELETE CASCADE,
        CONSTRAINT fk_allocations_inventory FOREIGN KEY (inventory_id) REFERENCES inventory (id) ON DELETE RESTRICT,
        CONSTRAINT chk_allocations_quantity CHECK (quantity > 0)
    );

-- ------------------------------------------------------------
CREATE TABLE
    payments (
        id SERIAL PRIMARY KEY,
        order_id INTEGER NOT NULL,
        method payment_method NOT NULL,
        status payment_status NOT NULL DEFAULT 'PENDING',
        amount DECIMAL(10, 2) NOT NULL,
        -- VNPay fields
        vnpay_txn_ref VARCHAR(100),
        vnpay_transaction_no VARCHAR(100),
        vnpay_bank_code VARCHAR(20),
        vnpay_response_code VARCHAR(10),
        paid_at TIMESTAMP,
        created_at TIMESTAMP NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE RESTRICT,
        CONSTRAINT chk_payments_amount CHECK (amount > 0)
    );

-- ------------------------------------------------------------
CREATE TABLE
    reviews (
        id SERIAL PRIMARY KEY,
        product_id INTEGER NOT NULL,
        customer_id INTEGER NOT NULL,
        order_item_id INTEGER NOT NULL,
        rating SMALLINT NOT NULL,
        comment TEXT,
        created_at TIMESTAMP NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
        CONSTRAINT fk_reviews_customer FOREIGN KEY (customer_id) REFERENCES accounts (id) ON DELETE RESTRICT,
        CONSTRAINT fk_reviews_order_item FOREIGN KEY (order_item_id) REFERENCES order_items (id) ON DELETE RESTRICT,
        CONSTRAINT uq_review UNIQUE (order_item_id),
        CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5)
    );