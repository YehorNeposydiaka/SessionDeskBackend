package org.example.Appointment.DTO;

import org.example.Appointment.Entity.AppointmentStatus;
import org.example.Appointment.Entity.BookingSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record SpecialistAppointmentSummary(
        UUID id,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status,
        UUID clientId,
        String clientName,
        String serviceName,
        BigDecimal price,
        boolean paid,
        BookingSource source
) { }
