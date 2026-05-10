-- ============================================================
-- V4__search_setup.sql
-- Postgres FTS for products: unaccent + pg_trgm
-- ============================================================

CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- unaccent() is STABLE, not IMMUTABLE → cannot be used directly in index expressions.
-- Wrapper marked IMMUTABLE so it can back GIN indexes.
CREATE OR REPLACE FUNCTION immutable_unaccent(text) RETURNS text
LANGUAGE sql IMMUTABLE STRICT PARALLEL SAFE
AS $$ SELECT unaccent('unaccent', $1) $$;

-- FTS index on name + description (brand/model keywords often live in description)
CREATE INDEX idx_products_fts ON products USING GIN (
    to_tsvector(
        'simple',
        immutable_unaccent(name || ' ' || COALESCE(description, ''))
    )
);

-- Trigram index on name for fuzzy match + autocomplete
CREATE INDEX idx_products_name_trgm ON products USING GIN (
    immutable_unaccent(lower(name)) gin_trgm_ops
);
