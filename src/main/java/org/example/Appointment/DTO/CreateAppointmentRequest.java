package org.example.Appointment.DTO;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record CreateAppointmentRequest(
        @NotNull UUID clientId,
        @NotNull LocalDate date,
        @NotNull LocalTime startTime,
        String note,
        UUID serviceId
        ){}
