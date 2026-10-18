package org.example.Service.DTO;

import org.example.Specialist.Entity.Specialist;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceResponse (
        UUID id,
        UUID specialistId,
        String serviceName,
        String description,
        BigDecimal price,
        int duration,
        boolean isActive,
        LocalDateTime createdAt
){}
