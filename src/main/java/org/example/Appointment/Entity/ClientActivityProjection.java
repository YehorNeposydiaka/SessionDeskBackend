package org.example.Appointment.Entity;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ClientActivityProjection {
    UUID getId();
    String getItemType();
    LocalDateTime getEventTime();
    String getTitleOrContent();
}