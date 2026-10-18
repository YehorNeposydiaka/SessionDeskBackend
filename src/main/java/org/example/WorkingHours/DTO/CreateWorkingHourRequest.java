package org.example.WorkingHours.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record CreateWorkingHourRequest (
        @NotBlank DayOfWeek day,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime
){}
