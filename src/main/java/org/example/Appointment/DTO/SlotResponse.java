package org.example.Appointment.DTO;

import java.time.LocalTime;

public record SlotResponse(
        LocalTime slot
) {}
