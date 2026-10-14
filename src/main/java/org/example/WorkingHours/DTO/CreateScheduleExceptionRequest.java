package org.example.WorkingHours.DTO;

import jakarta.validation.constraints.NotBlank;
import org.example.WorkingHours.Entity.ScheduleExceptionType;

import java.time.LocalDateTime;

public record CreateScheduleExceptionRequest (
    @NotBlank LocalDateTime startTime,
    @NotBlank LocalDateTime endTime,
    @NotBlank ScheduleExceptionType type
){}
