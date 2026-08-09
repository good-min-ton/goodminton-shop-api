-- ============================================================
-- V12__order_notifications_and_tracking.sql
--
-- An order changes hands three times on its way to the customer: SUPER_ADMIN
-- confirms, STORE_ADMIN prepares/ships/delivers, CUSTOMER confirms receipt.
-- Every handoff was silent, so an order sat in PENDING until somebody happened
-- to open the list. This adds the three things needed to stop that:
--
--   1. notifications  - tell the next role it is their turn
--   2. status_changed_at - how long an order has been where it is, so "stuck"
--      is a question the data can answer
--   3. lookup indexes - find an order by shipping code or phone when a customer
--      calls about one
-- ============================================================

-- ---------- 1. Notifications ----------
-- Recipients are resolved to concrete accounts when the notification is
-- emitted, one row each, rather than addressed to a role and joined at read
-- time. Roles here are tiny (one store admin per store, a handful of super
-- admins), and it keeps read_at a plain column instead of a second table.
CREATE TABLE
    notifications (
        id BIGSERIAL PRIMARY KEY,
        recipient_id INTEGER NOT NULL,
        order_id INTEGER NOT NULL,
        -- Enum-like, kept as text: notification kinds churn faster than the
        -- order lifecycle, and a Postgres enum cannot drop a value.
        type VARCHAR(40) NOT NULL,
        message VARCHAR(255) NOT NULL,
        read_at TIMESTAMP,
        created_at TIMESTAMP NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id)
            REFERENCES accounts (id) ON DELETE CASCADE,
        CONSTRAINT fk_notifications_order FOREIGN KEY (order_id)
            REFERENCES orders (id) ON DELETE CASCADE
    );

-- The unread badge polls this constantly: partial index so it stays small and
-- does not grow with read history.
CREATE INDEX idx_notifications_unread ON notifications (recipient_id, created_at DESC)
WHERE
    read_at IS NULL;

-- Full list for one recipient, newest first.
CREATE INDEX idx_notifications_recipient ON notifications (recipient_id, created_at DESC);

-- ---------- 2. How long an order has sat where it is ----------
-- orders carried only order_date, so "which orders are stuck" could not be
-- answered for any status past the first. Backfilled from order_date: for
-- existing rows that is the best known lower bound, and it never reports an
-- order as fresher than it is.
ALTER TABLE orders
ADD COLUMN status_changed_at TIMESTAMP NOT NULL DEFAULT NOW ();

UPDATE orders
SET
    status_changed_at = order_date;

-- Drives the "needs attention" queues: oldest first within a status.
CREATE INDEX idx_orders_status_changed ON orders (status, status_changed_at);

-- ---------- 3. Finding an order a customer is calling about ----------
-- Neither of these was searchable, so a complaint quoting a tracking code had
-- nowhere to go. Both are looked up exactly, not fuzzily.
CREATE INDEX idx_orders_shipping_code ON orders (shipping_code)
WHERE
    shipping_code IS NOT NULL;

CREATE INDEX idx_orders_recipient_phone ON orders (recipient_phone)
WHERE
    recipient_phone IS NOT NULL;

-- ---------- Note on RETURN_REQUESTED ----------
-- The order_status enum still carries RETURN_REQUESTED. Nothing sets it and no
-- transition leads to it, so no row can hold it; it is dropped from the Java
-- enum and from the admin filter in this change. The Postgres value is left
-- alone deliberately - removing an enum value requires recreating the type and
-- rewriting every dependent column, which is a real outage for a value that
-- costs nothing to keep.
