-- ============================================================
-- PARKSHARE - V16
-- REPLACE PARKING SPACE NUMBER WITH SPACE CODE
-- ============================================================

-- Make sure existing space codes are populated before making
-- the column required.
UPDATE parking_spaces
SET space_code = space_number
WHERE space_code IS NULL;

-- Make space_code mandatory.
ALTER TABLE parking_spaces
    ALTER COLUMN space_code SET NOT NULL;

-- Remove the old uniqueness constraint on space_number.
ALTER TABLE parking_spaces
DROP CONSTRAINT uq_parking_space_number;

-- Make space_code unique within each parking zone.
ALTER TABLE parking_spaces
    ADD CONSTRAINT uq_parking_space_code
        UNIQUE (parking_zone_id, space_code);

-- Remove the old space_number column.
ALTER TABLE parking_spaces
DROP COLUMN space_number;