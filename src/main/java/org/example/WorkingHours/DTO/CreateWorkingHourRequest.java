package org.example.WorkingHours.DTO;

import jakarta.validation.constraints.NotBlank;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record CreateWorkingHourRequest (
        @NotBlank DayOfWeek day,
        @NotBlank LocalTime startTime,
        @NotBlank LocalTime endTime
){}
