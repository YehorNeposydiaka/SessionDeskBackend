package org.example.WorkingHours.DTO;

import org.example.Specialist.Entity.Specialist;
import org.example.WorkingHours.Entity.ScheduleExceptionType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleExceptionResponse (
    UUID id,
    UUID specialistId,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    ScheduleExceptionType type
){}