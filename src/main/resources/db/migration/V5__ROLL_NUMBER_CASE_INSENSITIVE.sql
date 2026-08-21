-- Roll numbers are case-insensitive, and uppercase is the canonical form.
--
-- Every writer already normalises to uppercase, so uniqueness behaves correctly today.
-- But uq_volunteers_roll_number_active is a plain index: it is case-insensitive only
-- for as long as every caller remembers. A future path, a manual SQL fix or a data
-- import writing "21bcs001" alongside "21BCS001" would create two person records for
-- one human, silently, with attendance split between them.
--
-- These constraints make the invariant a guarantee rather than a convention.
--
-- Separate from V4 because V4 has already been applied; editing it in place would fail
-- Flyway's checksum validation on any database that has seen it.

-- Fold away any lowercase that predates the constraints. Skipped where an uppercase
-- twin already exists, so the unique index cannot be violated by the update itself;
-- anything left over is reported by the verification query at the bottom.
UPDATE volunteers v
SET roll_number = UPPER(v.roll_number),
    roll_number_normalized = UPPER(v.roll_number_normalized)
WHERE (v.roll_number <> UPPER(v.roll_number) OR v.roll_number_normalized <> UPPER(v.roll_number_normalized))
  AND NOT EXISTS (
      SELECT 1 FROM volunteers other
      WHERE other.pid <> v.pid
        AND other.deleted_at IS NULL
        AND other.roll_number_normalized = UPPER(v.roll_number_normalized)
  );

UPDATE volunteer_requests
SET roll_number = UPPER(roll_number),
    roll_number_normalized = UPPER(roll_number_normalized)
WHERE roll_number <> UPPER(roll_number)
   OR roll_number_normalized <> UPPER(roll_number_normalized);

ALTER TABLE volunteers
    ADD CONSTRAINT ck_volunteers_roll_number_upper
    CHECK (roll_number IS NULL OR roll_number = UPPER(roll_number));

ALTER TABLE volunteers
    ADD CONSTRAINT ck_volunteers_roll_number_normalized_upper
    CHECK (roll_number_normalized IS NULL OR roll_number_normalized = UPPER(roll_number_normalized));

ALTER TABLE volunteer_requests
    ADD CONSTRAINT ck_volunteer_requests_roll_number_upper
    CHECK (roll_number IS NULL OR roll_number = UPPER(roll_number));
