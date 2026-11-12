package org.example.Appointment.DTO;

import java.time.LocalDate;
import java.util.List;

public record AvailableSlotsResponse(
        LocalDate date,
        List<SlotResponse> slot
) {}
