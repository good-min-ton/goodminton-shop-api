-- ============================================================
-- V5__search_indexes_admin.sql
-- FTS + trigram indexes for admin/public search across:
-- categories, brands, accounts, stores.
-- Extensions + immutable_unaccent() are created in V4.
-- ============================================================
-- categories: search by name (description optional, low value here)
CREATE INDEX idx_categories_name_trgm ON categories USING GIN (immutable_unaccent (lower(name)) gin_trgm_ops);

-- brands: search by name
CREATE INDEX idx_brands_name_trgm ON brands USING GIN (immutable_unaccent (lower(name)) gin_trgm_ops);

-- accounts: search by full_name + email + phone (admin only)
CREATE INDEX idx_accounts_search_trgm ON accounts USING GIN (
    immutable_unaccent (lower(full_name || ' ' || email || ' ' || phone)) gin_trgm_ops
);

-- stores: search by name + address (admin only)
CREATE INDEX idx_stores_search_trgm ON stores USING GIN (
    immutable_unaccent (lower(name || ' ' || address)) gin_trgm_ops
);