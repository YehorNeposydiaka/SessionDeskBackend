package org.example.Appointment.DTO;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record RescheduleRequest(
        @NotNull UUID appointmentId,
        @NotNull LocalDate date,
        @NotNull LocalTime startTime
){}
