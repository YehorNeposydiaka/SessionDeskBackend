package org.example.WorkingHours.DTO;

import jakarta.validation.constraints.NotBlank;
import org.example.WorkingHours.Entity.ScheduleExceptionType;


import java.time.LocalTime;

public record CreateScheduleExceptionRequest (

    @NotBlank LocalTime startTime,
    @NotBlank LocalTime endTime,
    @NotBlank ScheduleExceptionType type
){}
