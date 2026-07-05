-- ============================================================
-- V9__add_payos_payment_method.sql
-- Add PAYOS to the payment_method enum + PayOS-specific tracking columns.
-- ============================================================

ALTER TYPE payment_method ADD VALUE IF NOT EXISTS 'PAYOS';

ALTER TABLE payments ADD COLUMN payos_order_code BIGINT;

ALTER TABLE payments ADD COLUMN payos_payment_link_id VARCHAR(64);

ALTER TABLE payments ADD COLUMN payos_reference VARCHAR(64);

CREATE INDEX idx_payments_payos_order_code ON payments (payos_order_code)
WHERE
    payos_order_code IS NOT NULL;
