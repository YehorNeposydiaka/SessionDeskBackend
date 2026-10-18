package org.example.WorkingHours.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.WorkingHours.Entity.ScheduleExceptionType;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateScheduleExceptionRequest(
        LocalTime startTime,
        LocalTime endTime,
        ScheduleExceptionType type
){}
