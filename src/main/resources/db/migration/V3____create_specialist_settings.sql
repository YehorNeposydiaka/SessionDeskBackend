CREATE TABLE specialist_settings (
                                     id UUID PRIMARY KEY,
                                     specialist_id UUID NOT NULL,
                                     max_booking_days_ahead INTEGER NOT NULL DEFAULT 30,
                                     min_booking_notice_hours INTEGER NOT NULL DEFAULT 2,
                                     service_id UUID,
                                     is_service_selectable BOOLEAN NOT NULL DEFAULT TRUE,

                                     CONSTRAINT fk_specialist_settings_specialist
                                         FOREIGN KEY (specialist_id)
                                             REFERENCES specialists (id)
                                             ON DELETE CASCADE,

                                     CONSTRAINT fk_specialist_settings_default_service
                                         FOREIGN KEY (service_id)
                                             REFERENCES services (id)
                                             ON DELETE SET NULL,

                                     CONSTRAINT uk_specialist_settings_specialist_id
                                         UNIQUE (specialist_id)
);