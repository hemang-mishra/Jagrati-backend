-- Identity restructure.
--
-- Numbered V4, not V3: V3__POSTS_FEATURE.sql lives on the feat-posts branch and is
-- already applied in at least one environment. Two migrations sharing a version number
-- fail Flyway validation on any database that has seen the other one.
--
-- A volunteer record stops being a side-effect of an account and becomes a person
-- record anchored to a roll number. Three things change:
--
--   1. volunteers.user_pid  -- nullable link to an account; NULL means "provisional",
--                              a real person known by roll number with no app account.
--   2. roll_number          -- normalized, uniquely held by one living volunteer,
--                              released on deletion (it is itself personal data).
--   3. deleted_at           -- real soft delete, replacing the in-place scrub that
--                              destroyed the roll number and could not be reasoned about.

-- ---------------------------------------------------------------------------
-- 1. Widen PID columns.
--    PidGenerator emits "<uuid>_<millis>" = exactly 50 chars against VARCHAR(50),
--    i.e. zero headroom. Widening varchar is metadata-only in Postgres.
-- ---------------------------------------------------------------------------

ALTER TABLE users ALTER COLUMN pid TYPE VARCHAR(64);
ALTER TABLE students ALTER COLUMN pid TYPE VARCHAR(64);
ALTER TABLE students ALTER COLUMN registered_by_pid TYPE VARCHAR(64);
ALTER TABLE volunteers ALTER COLUMN pid TYPE VARCHAR(64);
ALTER TABLE user_roles ALTER COLUMN user_pid TYPE VARCHAR(64);
ALTER TABLE user_roles ALTER COLUMN assigned_by_pid TYPE VARCHAR(64);
ALTER TABLE role_transitions ALTER COLUMN user_pid TYPE VARCHAR(64);
ALTER TABLE role_transitions ALTER COLUMN requested_by_pid TYPE VARCHAR(64);
ALTER TABLE role_transitions ALTER COLUMN approved_by_pid TYPE VARCHAR(64);
ALTER TABLE student_attendance ALTER COLUMN student_pid TYPE VARCHAR(64);
ALTER TABLE student_attendance ALTER COLUMN marked_by_user TYPE VARCHAR(64);
ALTER TABLE student_group_history ALTER COLUMN student_pid TYPE VARCHAR(64);
ALTER TABLE student_group_history ALTER COLUMN assigned_by_pid TYPE VARCHAR(64);
ALTER TABLE volunteer_attendance ALTER COLUMN volunteer_pid TYPE VARCHAR(64);
ALTER TABLE volunteer_attendance ALTER COLUMN marked_by_user TYPE VARCHAR(64);
ALTER TABLE volunteer_requests ALTER COLUMN requested_by_pid TYPE VARCHAR(64);
ALTER TABLE volunteer_requests ALTER COLUMN reviewed_by_pid TYPE VARCHAR(64);
ALTER TABLE fcm_tokens ALTER COLUMN pid TYPE VARCHAR(64);

-- The posts feature (V3) also holds pids, but it lands on its own branch and may not
-- have been applied here yet. Widen it only if it exists, so this migration does not
-- depend on merge order.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'post') THEN
        ALTER TABLE post ALTER COLUMN volunteer_pid TYPE VARCHAR(64);
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'post_likes') THEN
        ALTER TABLE post_likes ALTER COLUMN user_pid TYPE VARCHAR(64);
    END IF;
END $$;

-- ---------------------------------------------------------------------------
-- 2. Soft-delete columns.
-- ---------------------------------------------------------------------------

ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE users ADD COLUMN deleted_by_pid VARCHAR(64);
ALTER TABLE users ADD COLUMN deletion_source VARCHAR(20);

ALTER TABLE volunteers ADD COLUMN deleted_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE volunteers ADD COLUMN deleted_by_pid VARCHAR(64);
ALTER TABLE volunteers ADD COLUMN deletion_source VARCHAR(20);

ALTER TABLE students ADD COLUMN deleted_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE students ADD COLUMN deleted_by_pid VARCHAR(64);
ALTER TABLE students ADD COLUMN deletion_source VARCHAR(20);

ALTER TABLE volunteer_requests ADD COLUMN deleted_at TIMESTAMP WITHOUT TIME ZONE;

-- Rows already scrubbed by the old @SQLDelete are recognisable by the literal
-- names it wrote. Their original data is gone and cannot be recovered; all we
-- can do is record that they are deleted so they stop leaking into listings.
UPDATE users
SET deleted_at = updated_at, deletion_source = 'LEGACY'
WHERE is_active = false AND first_name = 'Deleted' AND last_name = 'User';

UPDATE volunteers
SET deleted_at = updated_at, deletion_source = 'LEGACY'
WHERE is_active = false AND first_name = 'Deleted' AND last_name = 'Volunteer';

UPDATE students
SET deleted_at = updated_at, deletion_source = 'LEGACY'
WHERE is_active = false AND first_name = 'Deleted' AND last_name = 'Student';

UPDATE volunteer_requests
SET deleted_at = updated_at
WHERE first_name = 'Deleted' AND last_name = 'Request';

-- ---------------------------------------------------------------------------
-- 3. Person / account split.
-- ---------------------------------------------------------------------------

-- A provisional record is created from a roll number alone, so the fields that only
-- a person can supply about themselves cannot be required. Gender gains an UNKNOWN
-- member rather than becoming nullable, which keeps it non-null through the DTOs.
ALTER TABLE volunteers ALTER COLUMN date_of_birth DROP NOT NULL;

ALTER TABLE volunteers ADD COLUMN user_pid VARCHAR(64);
ALTER TABLE volunteers ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE volunteers ADD COLUMN roll_number_normalized VARCHAR(32);
ALTER TABLE volunteers ADD COLUMN created_by_pid VARCHAR(64);

-- Every volunteer that exists today was created from an account, with pid == users.pid.
-- Preserve that one-to-one exactly.
UPDATE volunteers v
SET user_pid = v.pid
WHERE EXISTS (SELECT 1 FROM users u WHERE u.pid = v.pid);

ALTER TABLE volunteers
    ADD CONSTRAINT fk_volunteers_on_user_pid FOREIGN KEY (user_pid) REFERENCES users (pid);

ALTER TABLE volunteers
    ADD CONSTRAINT fk_volunteers_on_created_by_pid FOREIGN KEY (created_by_pid) REFERENCES users (pid);

-- One volunteer record per account.
CREATE UNIQUE INDEX uq_volunteers_user_pid ON volunteers (user_pid) WHERE user_pid IS NOT NULL;

CREATE INDEX idx_volunteers_status ON volunteers (status);

-- ---------------------------------------------------------------------------
-- 4. Roll number becomes the identity anchor.
-- ---------------------------------------------------------------------------

-- roll_number was VARCHAR(20), but an email local part can be longer than that
-- and the backfill below writes one straight into this column.
ALTER TABLE volunteers ALTER COLUMN roll_number TYPE VARCHAR(32);

-- Normalize what is already there.
UPDATE volunteers
SET roll_number = UPPER(TRIM(roll_number))
WHERE roll_number IS NOT NULL AND TRIM(roll_number) <> '';

UPDATE volunteers
SET roll_number = NULL
WHERE roll_number IS NOT NULL AND TRIM(roll_number) = '';

-- Backfill from the college address. The local part of an @iiitdmj.ac.in address
-- *is* the roll number, so any volunteer whose roll number was lost to the
-- no-fallback bug in VolunteerService.updateVolunteerDetails can be recovered.
-- Skipped where it would collide with a roll number already held.
UPDATE volunteers v
SET roll_number = UPPER(SPLIT_PART(u.email, '@', 1))
FROM users u
WHERE v.user_pid = u.pid
  AND v.roll_number IS NULL
  AND v.deleted_at IS NULL
  AND u.deleted_at IS NULL
  AND u.email LIKE '%@iiitdmj.ac.in'
  AND LENGTH(SPLIT_PART(u.email, '@', 1)) BETWEEN 1 AND 32
  AND NOT EXISTS (
      SELECT 1 FROM volunteers other
      WHERE other.pid <> v.pid
        AND other.deleted_at IS NULL
        AND other.roll_number = UPPER(SPLIT_PART(u.email, '@', 1))
  );

UPDATE volunteers
SET roll_number_normalized = roll_number
WHERE roll_number IS NOT NULL;

-- Deletion releases the roll number, so uniqueness only binds living volunteers.
ALTER TABLE volunteers DROP CONSTRAINT IF EXISTS uc_volunteers_roll_number;

CREATE UNIQUE INDEX uq_volunteers_roll_number_active
    ON volunteers (roll_number_normalized)
    WHERE deleted_at IS NULL AND roll_number_normalized IS NOT NULL;

-- Roll numbers are case-insensitive, and uppercase is the canonical form.
--
-- The uniqueness index below is a plain one, so it is only case-insensitive as long as
-- every writer uppercases. These constraints make that a guarantee rather than a
-- convention: anything storing "23bcs001" fails loudly instead of quietly creating a
-- second person alongside "23BCS001".
ALTER TABLE volunteers
    ADD CONSTRAINT ck_volunteers_roll_number_upper
    CHECK (roll_number IS NULL OR roll_number = UPPER(roll_number));

ALTER TABLE volunteers
    ADD CONSTRAINT ck_volunteers_roll_number_normalized_upper
    CHECK (roll_number_normalized IS NULL OR roll_number_normalized = UPPER(roll_number_normalized));

ALTER TABLE volunteer_requests
    ADD CONSTRAINT ck_volunteer_requests_roll_number_upper
    CHECK (roll_number IS NULL OR roll_number = UPPER(roll_number));

-- Required while alive, released on deletion.
--
-- NOT VALID: any volunteer still missing a roll number after the backfill above
-- (no college address on file, or a collision) is tolerated as-is but must supply
-- one before their row can be updated again. Login-time derivation fills these in.
-- Once the count reaches zero, a later migration can VALIDATE CONSTRAINT.
ALTER TABLE volunteers
    ADD CONSTRAINT ck_volunteers_roll_number_required
    CHECK (deleted_at IS NOT NULL OR roll_number IS NOT NULL) NOT VALID;

-- ---------------------------------------------------------------------------
-- 5. Volunteer requests carry the derived roll number.
-- ---------------------------------------------------------------------------

ALTER TABLE volunteer_requests ALTER COLUMN roll_number TYPE VARCHAR(32);
ALTER TABLE volunteer_requests ADD COLUMN roll_number_normalized VARCHAR(32);

UPDATE volunteer_requests
SET roll_number = UPPER(TRIM(roll_number)),
    roll_number_normalized = UPPER(TRIM(roll_number))
WHERE roll_number IS NOT NULL AND TRIM(roll_number) <> '';

CREATE INDEX idx_volunteer_requests_roll_number ON volunteer_requests (roll_number_normalized);

-- ---------------------------------------------------------------------------
-- 6. Deletion audit. Records that a deletion happened and who performed it,
--    carrying no personal data of its own.
-- ---------------------------------------------------------------------------

CREATE TABLE account_deletions
(
    id             BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    subject_pid    VARCHAR(64)                             NOT NULL,
    performed_by_pid VARCHAR(64),
    source         VARCHAR(20)                             NOT NULL,
    had_volunteer_record BOOLEAN                           NOT NULL,
    CONSTRAINT pk_account_deletions PRIMARY KEY (id)
);

CREATE INDEX idx_account_deletions_subject ON account_deletions (subject_pid);
