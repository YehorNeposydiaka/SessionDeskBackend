package org.example.Appointment.DTO;

import org.example.Appointment.Entity.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ClientAppointmentResponse(
        UUID id,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status,
        String specialistName,
        String serviceName,
        BigDecimal price,
        int duration,
        boolean paid
) { }
