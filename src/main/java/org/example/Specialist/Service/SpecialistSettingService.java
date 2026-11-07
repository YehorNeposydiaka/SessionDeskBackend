package org.example.Specialist.Service;

import jakarta.transaction.Transactional;
import org.example.Common.Exception.ApiException;
import org.example.Service.Repository.ServiceRepository;
import org.example.Specialist.DTO.CreateSettingsRequest;
import org.example.Specialist.DTO.SettingResponse;
import org.example.Specialist.DTO.UpdateSettingRequest;
import org.example.Specialist.Entity.Specialist;
import org.example.Specialist.Entity.SpecialistSettings;
import org.example.Specialist.Repository.SpecialistRepository;
import org.example.Specialist.Repository.SpecialistSettingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class SpecialistSettingService {
    private final SpecialistSettingRepository settingsRepository;
    private final SpecialistRepository specialistRepository;
    private final ServiceRepository serviceRepository;

    public SpecialistSettingService(SpecialistSettingRepository settingsRepository,
                                     SpecialistRepository specialistRepository,
                                     ServiceRepository serviceRepository) {
        this.settingsRepository = settingsRepository;
        this.specialistRepository = specialistRepository;
        this.serviceRepository = serviceRepository;
    }

    // Викликається з реєстрації спеціаліста: створює налаштування зі значеннями за замовчуванням
    @Transactional
    public SpecialistSettings createDefaultSettings(Specialist specialist) {
        return settingsRepository.save(SpecialistSettings.builder()
                .specialist(specialist)
                .build());
    }

    @Transactional
    public SettingResponse createSettings(UUID specialistId, CreateSettingsRequest request) {
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));

        if (settingsRepository.findBySpecialist_Id(specialistId).isPresent())
            throw new ApiException("Settings already exist", HttpStatus.CONFLICT);

        SpecialistSettings settings = SpecialistSettings.builder()
                .specialist(specialist)
                .build();

        if (request.maxBookingDaysAhead() != null)
            settings.setMaxBookingDaysAhead(request.maxBookingDaysAhead());
        if (request.minBookingNoticeHours() != null)
            settings.setMinBookingNoticeHours(request.minBookingNoticeHours());
        if (request.serviceSelectable() != null)
            settings.setServiceSelectable(request.serviceSelectable());

        org.example.Service.Entity.Service service = org.example.Service.Entity.Service.builder().serviceName(request.serviceName())
                .description(request.description())
                .price(request.price())
                .duration(request.duration())
                .isActive(true)
                .specialist(specialist).build();


        settings.setDefaultService(service);

        assertConsistent(settings);
        serviceRepository.save(service);
        return toResponse(settingsRepository.save(settings));
    }

    @Transactional
    public SettingResponse updateSettings(UUID specialistId, UpdateSettingRequest request) {
        SpecialistSettings settings = settingsRepository.findBySpecialist_Id(specialistId)
                .orElseThrow(() -> new ApiException("Settings not found", HttpStatus.NOT_FOUND));

        if (request.maxBookingDaysAhead() != null)
            settings.setMaxBookingDaysAhead(request.maxBookingDaysAhead());
        if (request.minBookingNoticeHours() != null)
            settings.setMinBookingNoticeHours(request.minBookingNoticeHours());
        if (request.serviceSelectable() != null)
            settings.setServiceSelectable(request.serviceSelectable());

        if (Boolean.TRUE.equals(request.clearDefaultService())) {
            if (request.defaultServiceId() != null)
                throw new ApiException("Cannot set and clear default service at the same time",
                        HttpStatus.BAD_REQUEST);
            settings.setDefaultService(null);
        } else if (request.defaultServiceId() != null) {
            settings.setDefaultService(findOwnActiveService(request.defaultServiceId(), specialistId));
        }

        assertConsistent(settings);
        return toResponse(settings);
    }

    @Transactional
    public SettingResponse getSettings(UUID specialistId) {
        return toResponse(settingsRepository.findBySpecialist_Id(specialistId)
                .orElseThrow(() -> new ApiException("Settings not found", HttpStatus.NOT_FOUND)));
    }

    private org.example.Service.Entity.Service findOwnActiveService(UUID serviceId, UUID specialistId) {
        org.example.Service.Entity.Service service = serviceRepository
                .findByIdAndSpecialist_Id(serviceId, specialistId)
                .orElseThrow(() -> new ApiException("Service not found", HttpStatus.NOT_FOUND));
        if (!service.isActive())
            throw new ApiException("Service is not active", HttpStatus.CONFLICT);
        return service;
    }

    private void assertConsistent(SpecialistSettings settings) {
        if (!settings.isServiceSelectable() && settings.getDefaultService() == null)
            throw new ApiException(
                    "Default service is required when service selection is disabled",
                    HttpStatus.CONFLICT);
    }

    private SettingResponse toResponse(SpecialistSettings s) {
        return new SettingResponse(
                s.getMaxBookingDaysAhead(),
                s.getMinBookingNoticeHours(),
                s.getDefaultService() != null ? s.getDefaultService().getId() : null,
                s.isServiceSelectable());
    }
}