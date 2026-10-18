package org.example.WorkingHours.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.WorkingHours.Entity.ScheduleExceptionType;


import java.time.LocalDate;
import java.time.LocalTime;

public record CreateScheduleExceptionRequest (
    @NotNull LocalDate date,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    @NotBlank ScheduleExceptionType type
){}
