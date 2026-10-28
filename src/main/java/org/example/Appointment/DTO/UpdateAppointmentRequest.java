package org.example.Appointment.DTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record UpdateAppointmentRequest(
       String note,
       LocalDate date,
       LocalTime startTime,
       UUID serviceId
) { }
