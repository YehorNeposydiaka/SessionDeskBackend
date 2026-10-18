package org.example.WorkingHours.DTO;

import java.time.LocalTime;

public record UpdateWorkingHoursRequest(
        LocalTime startTime,
        LocalTime endTime
){}
