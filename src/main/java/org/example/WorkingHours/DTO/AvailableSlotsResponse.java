package org.example.WorkingHours.DTO;

import java.time.LocalTime;

public record AvailableSlotsResponse (
        LocalTime slots
){}