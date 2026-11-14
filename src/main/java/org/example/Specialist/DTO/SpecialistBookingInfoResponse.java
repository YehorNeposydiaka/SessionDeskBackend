package org.example.Specialist.DTO;

public record SpecialistBookingInfoResponse(
        String avatarUrl,
        String fullName,
        String iban,
        String paymentDetails,
        int maxBookingDaysAhead,
        Integer minBookingNoticeHours,
        boolean serviceSelectable,
        PublicBookingServiceResponse defaultService
) {}