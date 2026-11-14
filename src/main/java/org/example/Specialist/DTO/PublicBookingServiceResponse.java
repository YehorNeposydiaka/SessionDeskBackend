package org.example.Specialist.DTO;

import java.math.BigDecimal;
import java.util.UUID;

public record PublicBookingServiceResponse(
        UUID id,
        String serviceName,
        String description,
        BigDecimal price,
        int duration
) {}