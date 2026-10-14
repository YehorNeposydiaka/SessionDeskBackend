package org.example.WorkingHours.DTO;

import org.example.Specialist.Entity.Specialist;
import org.example.WorkingHours.Entity.ScheduleExceptionType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleExceptionResponse (
    UUID id,
    Specialist specialist,
    LocalDateTime startTime,
    LocalDateTime endTime,
    ScheduleExceptionType type
){}