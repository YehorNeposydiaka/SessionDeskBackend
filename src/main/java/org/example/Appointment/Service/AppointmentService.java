package org.example.Appointment.Service;

import org.example.Appointment.DTO.ClientAppointmentResponse;
import org.example.Appointment.DTO.SpecialistAppointmentDetails;
import org.example.Appointment.DTO.CreateAppointmentRequest;
import org.example.Appointment.DTO.SpecialistAppointmentSummary;
import org.example.Appointment.Entity.Appointment;
import org.example.Appointment.Entity.AppointmentStatus;
import org.example.Appointment.Entity.BookingSource;
import org.example.Appointment.Repository.AppointmentRepository;
import org.example.Client.Entity.Client;
import org.example.Client.Repository.ClientRepository;
import org.example.Common.Exception.ApiException;
import org.example.Service.Repository.ServiceRepository;
import org.example.Specialist.Entity.Specialist;
import org.example.Specialist.Entity.SpecialistSettings;
import org.example.Specialist.Repository.SpecialistRepository;
import org.example.Specialist.Repository.SpecialistSettingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.UUID;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final SpecialistRepository specialistRepository;
    private final SpecialistSettingRepository specialistSettingRepository;
    private final ClientRepository clientRepository;
    private final AvailabilityService availabilityService;
    private final ServiceRepository serviceRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              SpecialistRepository specialistRepository,
                              SpecialistSettingRepository specialistSettingRepository,
                              ClientRepository clientRepository,
                              AvailabilityService availabilityService,
                              ServiceRepository serviceRepository){
        this.appointmentRepository = appointmentRepository;
        this.specialistRepository = specialistRepository;
        this.specialistSettingRepository = specialistSettingRepository;
        this.clientRepository = clientRepository;
        this.availabilityService = availabilityService;
        this.serviceRepository = serviceRepository;
    }

    @Transactional
    public ClientAppointmentResponse createAppointmentByClient(String slug, CreateAppointmentRequest request){
        Appointment appointment = createAppointmentInternal(slug, request, BookingSource.CLIENT);
        return toClientAppointmentResponse(appointment);
    }

    @Transactional
    public SpecialistAppointmentDetails createAppointmentBySpecialist(String slug, CreateAppointmentRequest request){
        Appointment appointment = createAppointmentInternal(slug, request, BookingSource.SPECIALIST);
        return toAppointmentDetails(appointment);
    }

    private Appointment createAppointmentInternal(String slug, CreateAppointmentRequest request, BookingSource source){
        Specialist specialist = specialistRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ApiException("Client not found",HttpStatus.NOT_FOUND));
        SpecialistSettings settings = specialistSettingRepository.findBySpecialist_Id(specialist.getId())
                .orElseThrow(() -> new ApiException("Settings not found",HttpStatus.NOT_FOUND));

        org.example.Service.Entity.Service service;
        if(request.serviceId() != null) {
            service = serviceRepository.findById(request.serviceId())
                    .orElseThrow(() -> new ApiException("Requested service is not found", HttpStatus.NOT_FOUND));
        }
        else {
            if (settings.getDefaultService() != null)
                service = settings.getDefaultService();
            else
                throw new ApiException("Default service is not configured", HttpStatus.CONFLICT);
        }

        if (!service.getSpecialist().getId().equals(specialist.getId()))
            throw new ApiException("Service does not belong to this specialist", HttpStatus.BAD_REQUEST);

        if(!service.isActive())
            throw new ApiException("Service does not active",HttpStatus.CONFLICT);

        long duration = service.getDuration();

        LocalTime endTime = request.startTime().plusMinutes(duration);
        if (duration <= 0) {
            throw new ApiException("Service duration must be positive", HttpStatus.CONFLICT);
        }

        Appointment appointment = Appointment.builder()
                .client(client)
                .specialist(specialist)
                .service(service)
                .date(request.date())
                .startTime(request.startTime())
                .endTime(endTime)
                .status(AppointmentStatus.PLANNED)
                .isPaid(false)
                .price(service.getPrice())
                .duration((int) duration)
                .source(source)
                .build();

        boolean available = switch (source) {
            case CLIENT -> availabilityService.availabilityForClient(request.date(), request.startTime(), endTime, specialist.getId());
            case SPECIALIST -> availabilityService.availabilityForSpecialist(request.date(), request.startTime(), endTime, specialist.getId());
        };
        if (!available)
            throw new ApiException("Time is booked", HttpStatus.CONFLICT);

        appointmentRepository.save(appointment);
        return appointment;
    }

   //mark as paid(toggle paid + attach bill)
    // complete cancel appointment
    // update appointment
    // get specialist appointment by period
    // get by client
    // get details(by id)

    @Transactional(readOnly = true)
    public SpecialistAppointmentDetails getAppointmentDetails(UUID specialistId, UUID appointmentId){
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiException("Appointment not found",HttpStatus.NOT_FOUND));
        if(!appointment.getSpecialist().getId().equals(specialist.getId()))
            throw new ApiException("Appointment not belongs to specialist", HttpStatus.FORBIDDEN);

        return toAppointmentDetails(appointment);
    }

    private SpecialistAppointmentDetails toAppointmentDetails(Appointment appointment){
        return new SpecialistAppointmentDetails(appointment.getId(),
                appointment.getClient().getId(),
                appointment.getSpecialist().getId(),
                appointment.getService().getId(),
                appointment.getClient().getName(),
                appointment.getService().getServiceName(),
                appointment.getDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus(),
                appointment.isPaid(),
                appointment.getReceiptUrl(),
                appointment.getPrice(),
                appointment.getDuration(),
                appointment.getNote(),
                appointment.getCreatedAt(),
                appointment.getSource()
                );
    }
    private ClientAppointmentResponse toClientAppointmentResponse(Appointment appointment) {
        return new ClientAppointmentResponse(
                appointment.getId(),
                appointment.getDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus(),
                appointment.getSpecialist().getFullName(),
                appointment.getService().getServiceName(),
                appointment.getPrice(),
                appointment.getDuration(),
                appointment.isPaid()
        );
    }

    private SpecialistAppointmentSummary toSpecialistSummary(Appointment appointment) {
        return new SpecialistAppointmentSummary(
                appointment.getId(),
                appointment.getDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus(),
                appointment.getClient().getId(),
                appointment.getClient().getName(),
                appointment.getService().getServiceName(),
                appointment.getPrice(),
                appointment.isPaid(),
                appointment.getSource()
        );
    }
}
