package org.example.Specialist.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record SpecialistResponse (
        UUID id,
        String email,
        String fullName,
        String avatarUrl,
        String paymentDetails,
        String iban,
        String slug,
        LocalDateTime createdAt
){}
