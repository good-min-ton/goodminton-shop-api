-- ENUM types
CREATE TYPE user_role AS ENUM ('SUPER_ADMIN', 'STORE_ADMIN', 'CUSTOMER');

CREATE TYPE account_status AS ENUM ('ACTIVE', 'INACTIVE');

CREATE TYPE product_status AS ENUM ('IN_STOCK', 'OUT_OF_STOCK');

CREATE TYPE order_status AS ENUM ('NEW', 'PAID', 'SHIPPED', 'COMPLETED', 'CANCEL');

CREATE TYPE payment_method AS ENUM ('COD', 'BANKING', 'VNPAY');

CREATE TYPE payment_status AS ENUM ('PENDING', 'PAID', 'FAILED');

CREATE TYPE size_type AS ENUM ('RACKET', 'NON_RACKET');

-- Tables
CREATE TABLE
    account (
        account_id SERIAL PRIMARY KEY,
        full_name VARCHAR(100) NOT NULL,
        phone VARCHAR(20) UNIQUE,
        email VARCHAR(100) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        role user_role NOT NULL,
        create_at TIMESTAMP NOT NULL,
        status account_status NOT NULL
    );

CREATE TABLE
    store (
        store_id SERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        address VARCHAR(255) NOT NULL,
        contact VARCHAR(20) NOT NULL,
        longitude DECIMAL(10, 7) NOT NULL,
        latitude DECIMAL(10, 7) NOT NULL,
        create_at TIMESTAMP NOT NULL,
        admin_id INTEGER NOT NULL REFERENCES account (account_id)
    );

CREATE TABLE
    category (
        category_id SERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        description TEXT NOT NULL
    );

CREATE TABLE
    brand (
        brand_id SERIAL PRIMARY KEY,
        brand_name VARCHAR(100) NOT NULL
    );

CREATE TABLE
    product (
        product_id SERIAL PRIMARY KEY,
        category_id INTEGER NOT NULL REFERENCES category (category_id),
        brand_id INTEGER NOT NULL REFERENCES brand (brand_id),
        name VARCHAR(100) NOT NULL,
        description TEXT,
        thumbnail_url VARCHAR(255) NOT NULL,
        create_at TIMESTAMP NOT NULL
    );

CREATE TABLE
    product_variant (
        variant_id SERIAL PRIMARY KEY,
        product_id INTEGER NOT NULL REFERENCES product (product_id),
        version_id INTEGER REFERENCES version (version_id),
        color_id INTEGER REFERENCES color (color_id),
        size_id INTEGER REFERENCES size (size_id),
        price DECIMAL(10, 2)
    );

CREATE TABLE
    version (
        version_id SERIAL PRIMARY KEY,
        name VARCHAR(100) UNIQUE NOT NULL
    );

CREATE TABLE
    color (
        color_id SERIAL PRIMARY KEY,
        name VARCHAR(50) UNIQUE NOT NULL
    );

CREATE TABLE
    size (
        size_id SERIAL PRIMARY KEY,
        name VARCHAR(50) UNIQUE NOT NULL,
        type size_type NOT NULL
    );

CREATE TABLE
    variant_image (
        image_id SERIAL PRIMARY KEY,
        variant_id INTEGER NOT NULL REFERENCES product_variant (variant_id),
        image_url VARCHAR(255) NOT NULL,
        sort_order INTEGER DEFAULT 0,
        create_at TIMESTAMP NOT NULL
    );

CREATE TABLE
    product_specification (
        spec_id SERIAL PRIMARY KEY,
        product_id INTEGER NOT NULL REFERENCES product (product_id),
        name VARCHAR(50) NOT NULL,
        value VARCHAR(255) NOT NULL
    );

CREATE TABLE
    inventory (
        inventory_id SERIAL PRIMARY KEY,
        store_id INTEGER NOT NULL REFERENCES store (store_id),
        variant_id INTEGER NOT NULL REFERENCES product_variant (variant_id),
        quantity INTEGER NOT NULL,
        updated_at TIMESTAMP NOT NULL
    );

CREATE TABLE
    product_discount (
        discount_id SERIAL PRIMARY KEY,
        variant_id INTEGER NOT NULL REFERENCES product_variant (variant_id),
        sale_price DECIMAL(10, 2) NOT NULL,
        start_time TIMESTAMP NOT NULL,
        end_time TIMESTAMP NOT NULL,
        create_at TIMESTAMP NOT NULL
    );

CREATE TABLE
    "order" (
        order_id SERIAL PRIMARY KEY,
        customer_id INTEGER NOT NULL REFERENCES account (account_id),
        order_date TIMESTAMP NOT NULL,
        total_amount DECIMAL(10, 2) NOT NULL,
        name VARCHAR(100),
        phone VARCHAR(20),
        address VARCHAR(100),
        email VARCHAR(50),
        note TEXT,
        status order_status NOT NULL
    );

CREATE TABLE
    order_item (
        order_item_id SERIAL PRIMARY KEY,
        order_id INTEGER NOT NULL REFERENCES "order" (order_id),
        variant_id INTEGER NOT NULL REFERENCES product_variant (variant_id),
        quantity INTEGER NOT NULL,
        unit_price DECIMAL(10, 2) NOT NULL
    );

CREATE TABLE
    payment (
        payment_id SERIAL PRIMARY KEY,
        order_id INTEGER NOT NULL REFERENCES "order" (order_id),
        method payment_method NOT NULL,
        status payment_status NOT NULL DEFAULT 'PENDING',
        amount DECIMAL(10, 2) NOT NULL,
        paid_at TIMESTAMP,
        transaction_code VARCHAR(100),
        create_at TIMESTAMP NOT NULL
    );

CREATE TABLE
    review (
        review_id SERIAL PRIMARY KEY,
        product_id INTEGER NOT NULL REFERENCES product (product_id),
        user_id INTEGER NOT NULL REFERENCES account (account_id),
        rating SMALLINT NOT NULL,
        comment TEXT,
        create_at TIMESTAMP NOT NULL
    );