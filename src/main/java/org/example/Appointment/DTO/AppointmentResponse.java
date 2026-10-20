package org.example.Appointment.DTO;

import org.example.Appointment.Entity.AppointmentStatus;
import org.example.Appointment.Entity.BookingSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID clientId,
        UUID specialistId,
        UUID serviceId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        AppointmentStatus status,
        boolean isPaid,
        String receiptUrl,
        BigDecimal price,
        int duration,
        String note,
        LocalDateTime createdAt,
        BookingSource source
) { }
