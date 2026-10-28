package org.example.Appointment.DTO;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record RescheduleRequest(
        @NotNull LocalDate date,
        @NotNull LocalTime startTime
){}
