-- Index for inventory table
CREATE INDEX idx_inventory_variant_size_id ON inventory (variant_size_id);
CREATE INDEX idx_inventory_store_variant ON inventory (store_id, variant_size_id);

-- Index for order table
CREATE INDEX idx_order_status ON "order" (status);

-- Index for order_item table
CREATE INDEX idx_order_item_order_id ON order_item (order_id);

-- Index for order_item_inventory_allocation table
CREATE INDEX idx_allocation_order_item_id ON order_item_inventory_allocation (order_item_id);

-- Index for payment table
CREATE INDEX idx_payment_order_id ON payment (order_id);

-- Index for product_variant table
CREATE INDEX idx_product_variant_product_version_color ON product_variant (product_id, version_id, color_id);

-- Index for variant_size table
CREATE INDEX idx_variant_size_variant_size ON variant_size (variant_id, size_id);

-- Index for product_discount table
CREATE INDEX idx_product_discount_variant_size_id ON product_discount (variant_size_id);
CREATE INDEX idx_product_discount_time ON product_discount (start_time, end_time);

-- Index for review table
CREATE INDEX idx_review_product_id ON review (product_id);