package org.example.Specialist.DTO;

import java.util.UUID;

public record SettingResponse(
        int maxBookingDaysAhead,
        int minBookingNoticeHours,
        UUID defaultServiceId,
        boolean serviceSelectable
) { }