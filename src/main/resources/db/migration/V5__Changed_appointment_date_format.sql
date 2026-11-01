-- appointments: нова колонка date, заповнена з наявних start_time
ALTER TABLE appointments ADD COLUMN date DATE;
UPDATE appointments SET date = start_time::date;
ALTER TABLE appointments ALTER COLUMN date SET NOT NULL;

-- start_time / end_time: timestamp -> time
ALTER TABLE appointments
    ALTER COLUMN start_time TYPE TIME(6) USING start_time::time,
    ALTER COLUMN end_time   TYPE TIME(6) USING end_time::time;

-- appointments: джерело запису
ALTER TABLE appointments ADD COLUMN source VARCHAR(20);
UPDATE appointments SET source = 'SPECIALIST' WHERE source IS NULL;
ALTER TABLE appointments ALTER COLUMN source SET NOT NULL;
ALTER TABLE appointments
    ADD CONSTRAINT chk_appointments_source CHECK (source IN ('CLIENT','SPECIALIST'));

-- notes: текст нотатки
ALTER TABLE notes ADD COLUMN body VARCHAR(500);