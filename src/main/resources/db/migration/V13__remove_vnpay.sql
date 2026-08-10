-- ============================================================
-- V13__remove_vnpay.sql
-- Gỡ hoàn toàn VNPay khỏi lược đồ. Hệ thống chỉ còn COD và PayOS.
-- ============================================================

-- PostgreSQL không cho xoá một giá trị khỏi kiểu enum, nên phải dựng lại kiểu.
-- Trước khi đổi, chuyển các bản ghi cũ (nếu có) sang BANKING - phương thức
-- chuyển khoản chung. Không gán sang PAYOS vì đó là một cổng khác, ghi như vậy
-- sẽ làm sai lịch sử giao dịch.
UPDATE payments SET method = 'BANKING' WHERE method = 'VNPAY';

ALTER TYPE payment_method RENAME TO payment_method_legacy;

CREATE TYPE payment_method AS ENUM ('COD', 'BANKING', 'PAYOS');

ALTER TABLE payments
    ALTER COLUMN method TYPE payment_method
    USING method::text::payment_method;

DROP TYPE payment_method_legacy;

-- Chỉ mục và các cột riêng của VNPay không còn nơi nào ghi vào.
DROP INDEX IF EXISTS idx_payments_vnpay_txn_ref;

ALTER TABLE payments DROP COLUMN IF EXISTS vnpay_txn_ref;

ALTER TABLE payments DROP COLUMN IF EXISTS vnpay_transaction_no;

ALTER TABLE payments DROP COLUMN IF EXISTS vnpay_bank_code;

ALTER TABLE payments DROP COLUMN IF EXISTS vnpay_response_code;
