ALTER TABLE schedule_exceptions ADD COLUMN date DATE NOT NULL;
ALTER TABLE schedule_exceptions DROP COLUMN day_of_week;