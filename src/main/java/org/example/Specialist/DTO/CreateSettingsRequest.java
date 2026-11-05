package org.example.Specialist.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.UUID;

public record CreateSettingsRequest(
        @Min(1) @Max(365) Integer maxBookingDaysAhead,
        @Min(0) @Max(720) Integer minBookingNoticeHours,
        UUID defaultServiceId,
        Boolean serviceSelectable
) { }
