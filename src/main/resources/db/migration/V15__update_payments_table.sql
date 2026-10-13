ALTER TABLE payments
    ADD COLUMN payment_reference VARCHAR(50);

ALTER TABLE payments
    ADD COLUMN transaction_reference VARCHAR(100);

ALTER TABLE payments
    ADD CONSTRAINT uq_payments_payment_reference
        UNIQUE (payment_reference);

ALTER TABLE payments
    ADD CONSTRAINT uq_payments_transaction_reference
        UNIQUE (transaction_reference);

ALTER TABLE payments
    ALTER COLUMN payment_reference SET NOT NULL;