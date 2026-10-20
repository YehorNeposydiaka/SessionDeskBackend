package org.example.Appointment.DTO;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAppointmentRequest(
        @NotNull UUID clientId,
        @NotNull LocalDateTime startTime,
        String note,
        UUID serviceId
        ){}
