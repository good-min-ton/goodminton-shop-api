-- ============================================================
-- V11__enforce_single_central_store.sql
-- "Exactly one central store" was enforced only by application code
-- (StoreServiceImpl.demoteCurrentCentral). Nothing stopped a second row from
-- being flagged, and InventoryService.findCentralStore() reads it through
-- Optional<Store> — two rows would raise IncorrectResultSizeDataAccessException
-- and every ONLINE order would fail, far from the write that caused it.
--
-- A partial unique index rejects the bad write instead. It permits any number of
-- non-central stores (is_central = false) while allowing at most one true.
-- ============================================================

-- Fail loudly during migration if the data is already inconsistent, rather than
-- letting CREATE UNIQUE INDEX report it as an opaque constraint violation.
DO $$
DECLARE
    central_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO central_count FROM stores WHERE is_central;
    IF central_count > 1 THEN
        RAISE EXCEPTION
            'Cannot enforce a single central store: % stores have is_central = true. '
            'Demote all but one, then re-run the migration.', central_count;
    END IF;
END $$;

CREATE UNIQUE INDEX uq_stores_single_central ON stores (is_central)
WHERE
    is_central;
