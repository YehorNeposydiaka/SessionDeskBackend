package org.example.Appointment.Service;

import org.example.Appointment.DTO.AppointmentResponse;
import org.example.Appointment.DTO.CreateAppointmentRequest;
import org.example.Appointment.Repository.AppointmentRepository;
import org.example.Client.Repository.ClientRepository;
import org.example.Common.Exception.ApiException;
import org.example.Specialist.Entity.Specialist;
import org.example.Specialist.Repository.SpecialistRepository;
import org.example.Specialist.Repository.SpecialistSettingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final SpecialistRepository specialistRepository;
    private final SpecialistSettingRepository specialistSettingRepository;
    private final ClientRepository clientRepository;
    private final AvailabilityService availabilityService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              SpecialistRepository specialistRepository,
                              SpecialistSettingRepository specialistSettingRepository,
                              ClientRepository clientRepository,
                              AvailabilityService availabilityService){
        this.appointmentRepository = appointmentRepository;
        this.specialistRepository = specialistRepository;
        this.specialistSettingRepository = specialistSettingRepository;
        this.clientRepository = clientRepository;
        this.availabilityService = availabilityService;
    }

    @Transactional
    public AppointmentResponse createAppointment(String slug, CreateAppointmentRequest request){
        Specialist specialist = specialistRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));

    }
}
