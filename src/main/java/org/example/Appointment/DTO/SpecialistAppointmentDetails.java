package org.example.Appointment.DTO;

import org.example.Appointment.Entity.AppointmentStatus;
import org.example.Appointment.Entity.BookingSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record SpecialistAppointmentDetails(
        UUID id,
        UUID clientId,
        UUID specialistId,
        UUID serviceId,
        String clientName,
        String serviceName,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status,
        boolean paid,
        String receiptUrl,
        BigDecimal price,
        int duration,
        String note,
        LocalDateTime createdAt,
        BookingSource source
) { }
