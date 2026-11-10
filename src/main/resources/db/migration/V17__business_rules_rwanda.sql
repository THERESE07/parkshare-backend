-- ============================================================
-- PARKSHARE - V17
-- Business rules: payment status fix, VAT breakdown, overstay,
-- and database-level protection against double booking.
-- ============================================================

-- 1. The Java PaymentStatus enum uses PAID, but the original
--    constraint only allowed SUCCESS, so marking a payment paid failed.
ALTER TABLE payments DROP CONSTRAINT chk_payment_status;
UPDATE payments SET status = 'PAID' WHERE status = 'SUCCESS';
ALTER TABLE payments
    ADD CONSTRAINT chk_payment_status CHECK (
        status IN ('PENDING', 'PROCESSING', 'PAID', 'FAILED', 'CANCELLED', 'REFUNDED')
    );

-- 1b. Mobile-money number that paid, so refunds go back to the same wallet.
ALTER TABLE payments ADD COLUMN payer_phone VARCHAR(30);

-- 2. Store the VAT (18%, included in the price) and platform commission
--    computed when the reservation was priced, for receipts and payouts.
ALTER TABLE reservations
    ADD COLUMN vat_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN platform_fee NUMERIC(12, 2) NOT NULL DEFAULT 0.00;

-- 3. Overstay charged at check-out.
ALTER TABLE check_outs
    ADD COLUMN overstay_minutes INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN overstay_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    ADD CONSTRAINT chk_check_out_overstay CHECK (overstay_minutes >= 0 AND overstay_amount >= 0);

-- 4. A space can never hold two active reservations for overlapping times,
--    even if two requests arrive at the same moment.
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE reservations
    ADD CONSTRAINT excl_reservation_space_overlap
    EXCLUDE USING gist (
        parking_space_id WITH =,
        tstzrange(start_time, end_time, '[)') WITH &&
    )
    WHERE (parking_space_id IS NOT NULL AND status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN'));

-- 5. Fast lookup for the scheduled jobs.
CREATE INDEX idx_reservations_status_created ON reservations(status, created_at);
