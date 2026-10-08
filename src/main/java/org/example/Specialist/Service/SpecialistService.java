package org.example.Specialist.Service;

import org.example.Common.Exception.ApiException;
import org.example.Specialist.DTO.ChangePasswordRequest;
import org.example.Specialist.DTO.SpecialistResponse;
import org.example.Specialist.DTO.UpdateSpecialistRequest;
import org.example.Specialist.Entity.Specialist;
import org.example.Specialist.Repository.SpecialistRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SpecialistService {
    private final SpecialistRepository specialistRepository;
    private final PasswordEncoder passwordEncoder;

    public SpecialistService(SpecialistRepository specialistRepository,
                             PasswordEncoder passwordEncoder){
        this.specialistRepository = specialistRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public SpecialistResponse getSpecialistById(UUID specialistId){
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        return toSpecialistResponse(specialist);
    }
    public SpecialistResponse getSpecialistBySlug(String slug){
        Specialist specialist = specialistRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        return toSpecialistResponse(specialist);
    }

    @Transactional
    public SpecialistResponse updateSpecialist(UUID specialistId, UpdateSpecialistRequest request){
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));

        if(request.fullName() != null) specialist.setFullName(request.fullName());
        if(request.description() != null) specialist.setDescription(request.description());
        if(request.avatarUrl() != null) specialist.setAvatarUrl(request.avatarUrl());
        if(request.paymentDetails() != null) specialist.setPaymentDetails(request.paymentDetails());
        if(request.iban() != null) specialist.setIban(request.iban());
        if(request.slug() != null) specialist.setSlug(request.slug());

        specialistRepository.save(specialist);
        return toSpecialistResponse(specialist);
    }

    @Transactional
    public void changeSpecialistPassword(UUID specialistId, ChangePasswordRequest request){
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.currentPassword(), specialist.getPasswordHash())) {
            throw new ApiException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        if (request.currentPassword().equals(request.newPassword())) {
            throw new ApiException("New password must differ from current", HttpStatus.BAD_REQUEST);
        }

        specialist.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        specialistRepository.save(specialist);
    }

    @Transactional
    public void deleteSpecialist(UUID specialistId){
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        specialistRepository.delete(specialist);
    }

    //Method converts entity to DTO response
    private SpecialistResponse toSpecialistResponse(Specialist specialist){
        return new SpecialistResponse(
                specialist.getId(),
                specialist.getEmail(),
                specialist.getFullName(),
                specialist.getAvatarUrl(),
                specialist.getPaymentDetails(),
                specialist.getIban(),
                specialist.getSlug(),
                specialist.getCreatedAt()
        );
    }
}
