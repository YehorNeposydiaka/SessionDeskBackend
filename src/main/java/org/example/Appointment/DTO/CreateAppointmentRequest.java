package org.example.Appointment.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.Appointment.Entity.BookingSource;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAppointmentRequest(
        @NotNull UUID clientId,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        String note,
        @NotBlank BookingSource source
        ){}
