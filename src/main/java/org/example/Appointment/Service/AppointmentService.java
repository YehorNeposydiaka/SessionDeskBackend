package org.example.Appointment.Service;

import org.example.Appointment.DTO.*;
import org.example.Appointment.Entity.Appointment;
import org.example.Appointment.Entity.AppointmentStatus;
import org.example.Appointment.Entity.BookingSource;
import org.example.Appointment.Entity.ClientActivityProjection;
import org.example.Appointment.Repository.AppointmentRepository;
import org.example.Client.Entity.Client;
import org.example.Client.Repository.ClientRepository;
import org.example.Common.Exception.ApiException;
import org.example.Service.Repository.ServiceRepository;
import org.example.Specialist.Entity.Specialist;
import org.example.Specialist.Entity.SpecialistSettings;
import org.example.Specialist.Repository.SpecialistRepository;
import org.example.Specialist.Repository.SpecialistSettingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.example.Appointment.Entity.AppointmentStatus.COMPLETED;

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

    @Transactional
    public SpecialistAppointmentDetails toggleCompleteAppointment(UUID specialistId, UUID appointmentId){
        Appointment appointment = appointmentRepository.findByIdAndSpecialistId(appointmentId, specialistId)
                .orElseThrow(() -> new ApiException("Appointment not found",HttpStatus.NOT_FOUND));
        switch (appointment.getStatus()) {
            case COMPLETED -> appointment.setStatus(AppointmentStatus.PLANNED);
            case PLANNED -> appointment.setStatus(COMPLETED);
            default -> {}
        }
        return toAppointmentDetails(appointment);
    }

    public List<SpecialistAppointmentSummary> getAllAppointmentsForSpecialist(UUID specialistId, LocalDate startDate, LocalDate endDate){
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        List<Appointment> appointments = appointmentRepository.findAllBySpecialistIdAndDateBetween(specialistId, startDate, endDate);

        return appointments.stream().map(this::toSpecialistSummary).toList();
    }

    @Transactional
    public SpecialistAppointmentDetails attachBillToAppointment(UUID specialistId, UUID appointmentId, AttachBillToAppointmentRequest request){
        Appointment appointment = appointmentRepository.findByIdAndSpecialistId(appointmentId, specialistId)
                .orElseThrow(() -> new ApiException("Appointment not found", HttpStatus.NOT_FOUND));
        appointment.setReceiptUrl(request.billLink());
        appointment.setPaid(true);
        appointmentRepository.save(appointment);

        return toAppointmentDetails(appointment);
    }

    @Transactional
    public SpecialistAppointmentDetails togglePaid(UUID specialistId, UUID appointmentId){
        Appointment appointment = appointmentRepository.findByIdAndSpecialistId(appointmentId, specialistId)
                .orElseThrow(() -> new ApiException("Appointment not found", HttpStatus.NOT_FOUND));

        if (appointment.isPaid()) {
            if (appointment.getReceiptUrl() != null) {
                throw new ApiException("You must delete bill before setting unpaid", HttpStatus.FORBIDDEN);
            }
        }

        boolean newPaidStatus = !appointment.isPaid();
        appointment.setPaid(newPaidStatus);

        appointmentRepository.save(appointment);

        return toAppointmentDetails(appointment);
    }

    public Page<ClientActivityProjection> getClientAppointmentsAndNotes(UUID specialistId, UUID clientId, int page) {
        Pageable pageable = PageRequest.of(page, 10);

        return appointmentRepository.findClientActivities(specialistId, clientId, pageable);
    }

    @Transactional
    public SpecialistAppointmentDetails toggleCancelAppointmentBySpecialist(UUID specialistId, UUID appointmentId){
        Appointment appointment = appointmentRepository.findByIdAndSpecialistId(appointmentId, specialistId)
                .orElseThrow(() -> new ApiException("Appointment not found", HttpStatus.NOT_FOUND));

        switch (appointment.getStatus()) {
            case PLANNED -> appointment.setStatus(AppointmentStatus.CANCELLED);
            case CANCELLED -> {
                boolean free = availabilityService.availabilityForSpecialist(
                        appointment.getDate(), appointment.getStartTime(), appointment.getEndTime(),
                        specialistId, appointment.getId());
                if (!free)
                    throw new ApiException("Time is already taken, cannot restore appointment", HttpStatus.CONFLICT);
                appointment.setStatus(AppointmentStatus.PLANNED);
            }
            default -> throw new ApiException(
                    "Appointment with status " + appointment.getStatus() + " cannot be cancelled or restored",
                    HttpStatus.CONFLICT);
        }
        return toAppointmentDetails(appointment);
    }

    @Transactional
    public ClientAppointmentResponse rescheduleAppointmentByClient(UUID clientId, RescheduleRequest request){
        Appointment appointment = appointmentRepository.findByIdAndClientId(request.appointmentId(), clientId)
                .orElseThrow(() -> new ApiException("Appointment not found",HttpStatus.NOT_FOUND));
        org.example.Service.Entity.Service service = serviceRepository.findById(appointment.getService().getId())
                .orElseThrow(() -> new ApiException("Service not found",HttpStatus.NOT_FOUND));
        SpecialistSettings settings = specialistSettingRepository
                .findBySpecialist_Id(appointment.getSpecialist().getId())
                .orElseThrow(() -> new ApiException("Settings not found", HttpStatus.NOT_FOUND));

        long duration = service.getDuration();

        LocalTime endTime = request.startTime().plusMinutes(duration);
        if (duration <= 0) {
            throw new ApiException("Service duration must be positive", HttpStatus.CONFLICT);
        }
        if(!availabilityService.availabilityForClient(request.date(), request.startTime(), endTime, appointment.getSpecialist().getId(), appointment.getId()))
            throw new ApiException("Time is already booked",HttpStatus.CONFLICT);

        if (appointment.getStatus() != AppointmentStatus.PLANNED)
            throw new ApiException("Only planned appointments can be rescheduled", HttpStatus.CONFLICT);

        LocalDateTime start = LocalDateTime.of(appointment.getDate(), appointment.getStartTime());
        LocalDateTime deadline = start.minusHours(settings.getMinBookingNoticeHours());

        if (LocalDateTime.now().isAfter(deadline))
            throw new ApiException(
                    "Appointment can be rescheduled no later than " + settings.getMinBookingNoticeHours() + " hours before start",
                    HttpStatus.CONFLICT);

        appointment.setDate(request.date());
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(endTime);

        appointmentRepository.save(appointment);
        return  toClientAppointmentResponse(appointment);
    }
    // update appointment

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

    @Transactional
    public ClientAppointmentResponse cancelAppointmentByClient(UUID clientId, UUID appointmentId) {
        Appointment appointment = appointmentRepository.findByIdAndClientId(appointmentId, clientId)
                .orElseThrow(() -> new ApiException("Appointment not found", HttpStatus.NOT_FOUND));

        if (appointment.getStatus() != AppointmentStatus.PLANNED)
            throw new ApiException("Only planned appointments can be cancelled", HttpStatus.CONFLICT);

        SpecialistSettings settings = specialistSettingRepository
                .findBySpecialist_Id(appointment.getSpecialist().getId())
                .orElseThrow(() -> new ApiException("Settings not found", HttpStatus.NOT_FOUND));

        LocalDateTime start = LocalDateTime.of(appointment.getDate(), appointment.getStartTime());
        LocalDateTime deadline = start.minusHours(settings.getMinBookingNoticeHours());

        if (LocalDateTime.now().isAfter(deadline))
            throw new ApiException(
                    "Appointment can be cancelled no later than " + settings.getMinBookingNoticeHours() + " hours before start",
                    HttpStatus.CONFLICT);

        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toClientAppointmentResponse(appointment);
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
            case CLIENT -> {
                long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), request.date());
                if((int) daysBetween > settings.getMaxBookingDaysAhead()){
                    throw new ApiException("The limit of days for booking has been exceeded",HttpStatus.FORBIDDEN);
                }
                yield availabilityService.availabilityForClient(request.date(), request.startTime(), endTime, specialist.getId(), null);
            }
            case SPECIALIST -> availabilityService.availabilityForSpecialist(request.date(), request.startTime(), endTime, specialist.getId(), null);
        };
        if (!available)
            throw new ApiException("Time is booked", HttpStatus.CONFLICT);

        appointmentRepository.save(appointment);
        return appointment;
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
