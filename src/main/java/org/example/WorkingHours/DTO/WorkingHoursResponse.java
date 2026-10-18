package org.example.WorkingHours.DTO;

import org.example.Specialist.Entity.Specialist;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record WorkingHoursResponse (
    UUID id,
    UUID specialistId,
    DayOfWeek day,
    LocalTime startTime,
    LocalTime endTime
){}
