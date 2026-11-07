package org.example.Specialist.DTO;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateSettingsRequest(
        @Min(1) @Max(365) Integer maxBookingDaysAhead,
        @Min(0) @Max(720) Integer minBookingNoticeHours,
        Boolean serviceSelectable,
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100,
                message = "Name must be between 2 and 100 characters")
        String serviceName,

        @Size(max = 1000,
                message = "Description must not exceed 1000 characters")
        String description,

        @PositiveOrZero(message = "Ціна не може бути меншою за 0")
        BigDecimal price,

        @NotNull(message = "Duration is required")
        @Min(value = 15, message = "Minimal duration — 15 minutes")
        int duration
) { }
