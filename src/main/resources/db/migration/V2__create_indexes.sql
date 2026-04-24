-- ============================================================
-- V2__create_indexes.sql
-- Badminton Store System — Indexes
-- ============================================================
-- ------------------------------------------------------------
-- accounts
-- ------------------------------------------------------------
-- Tìm kiếm tài khoản theo role + status (admin filter)
CREATE INDEX idx_accounts_role_status ON accounts (role, status);

-- ------------------------------------------------------------
-- stores
-- ------------------------------------------------------------
-- Lookup store theo admin
CREATE INDEX idx_stores_admin_id ON stores (admin_id);

-- ------------------------------------------------------------
-- products
-- ------------------------------------------------------------
-- Listing page: filter theo category + brand + visibility
CREATE INDEX idx_products_category_id ON products (category_id);

CREATE INDEX idx_products_brand_id ON products (brand_id);

CREATE INDEX idx_products_is_visible ON products (is_visible);

-- Nhóm các sản phẩm liên quan (related versions)
CREATE INDEX idx_products_related_id ON products (related_product_id)
WHERE
    related_product_id IS NOT NULL;

-- ------------------------------------------------------------
-- product_specifications
-- ------------------------------------------------------------
CREATE INDEX idx_specs_product_id ON product_specifications (product_id);

-- ------------------------------------------------------------
-- product_variants
-- ------------------------------------------------------------
-- Lookup variant theo product (join thường xuyên)
CREATE INDEX idx_variants_product_id ON product_variants (product_id);

-- ------------------------------------------------------------
-- resources
-- ------------------------------------------------------------
CREATE INDEX idx_resources_owner ON resources (owner_type, owner_id, sort_order);

-- ------------------------------------------------------------
-- product_variants — variant đang giảm giá
-- ------------------------------------------------------------
CREATE INDEX idx_variants_sale_price ON product_variants (sale_price)
WHERE
    sale_price IS NOT NULL;

-- ------------------------------------------------------------
-- inventory
-- ------------------------------------------------------------
-- Lookup tồn kho theo store (store admin xem inventory)
CREATE INDEX idx_inventory_store_id ON inventory (store_id);

-- Tìm các store còn hàng của 1 variant (auto-assign kho)
CREATE INDEX idx_inventory_variant_id ON inventory (variant_id);

-- ------------------------------------------------------------
-- orders
-- ------------------------------------------------------------
-- Lịch sử đơn hàng của customer
CREATE INDEX idx_orders_customer_id ON orders (customer_id);

-- Đơn hàng theo store (store admin xem)
CREATE INDEX idx_orders_store_id ON orders (store_id)
WHERE
    store_id IS NOT NULL;

-- Filter theo status (pending orders, shipping,...)
CREATE INDEX idx_orders_status ON orders (status);

-- Filter theo loại đơn
CREATE INDEX idx_orders_type ON orders (order_type);

-- Dashboard: filter theo ngày
CREATE INDEX idx_orders_order_date ON orders (order_date);

-- ------------------------------------------------------------
-- order_items
-- ------------------------------------------------------------
CREATE INDEX idx_order_items_order_id ON order_items (order_id);

CREATE INDEX idx_order_items_variant_id ON order_items (variant_id);

-- ------------------------------------------------------------
-- payments
-- ------------------------------------------------------------
-- Lookup payment theo order
CREATE INDEX idx_payments_order_id ON payments (order_id);

-- Filter theo status + method (admin report)
CREATE INDEX idx_payments_status_method ON payments (status, method);

-- Dashboard: filter theo ngày thanh toán
CREATE INDEX idx_payments_paid_at ON payments (paid_at)
WHERE
    paid_at IS NOT NULL;

-- Tra cứu giao dịch VNPay
CREATE INDEX idx_payments_vnpay_txn_ref ON payments (vnpay_txn_ref)
WHERE
    vnpay_txn_ref IS NOT NULL;

-- ------------------------------------------------------------
-- reviews
-- ------------------------------------------------------------
-- Lấy review theo product (listing review)
CREATE INDEX idx_reviews_product_id ON reviews (product_id);

-- Tính average rating theo product (aggregate)
CREATE INDEX idx_reviews_product_rating ON reviews (product_id, rating);