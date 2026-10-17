ALTER TABLE schedule_exceptions
    DROP COLUMN IF EXISTS start_time,
    DROP COLUMN IF EXISTS end_time;

ALTER TABLE schedule_exceptions
    ADD COLUMN day_of_week VARCHAR(10) NOT NULL,
    ADD COLUMN start_time TIME(6) NOT NULL,
    ADD COLUMN end_time TIME(6) NOT NULL;

ALTER TABLE schedule_exceptions
    ADD CONSTRAINT chk_schedule_exceptions_day_of_week
    CHECK (day_of_week in ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY'));