-- ============================================================
-- V6__allow_walkin_orders.sql
-- IN_STORE orders can be created for walk-in customers without an account.
-- Walk-in info (name/phone) reuses existing recipient_name / recipient_phone.
-- ============================================================

ALTER TABLE orders ALTER COLUMN customer_id DROP NOT NULL;
