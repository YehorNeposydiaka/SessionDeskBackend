package org.example.Appointment.Service;

import org.example.Appointment.DTO.*;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private SpecialistRepository specialistRepository;
    @Mock private SpecialistSettingRepository specialistSettingRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private AvailabilityService availabilityService;
    @Mock private ServiceRepository serviceRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private final UUID specialistId = UUID.randomUUID();
    private final UUID clientId = UUID.randomUUID();
    private final UUID appointmentId = UUID.randomUUID();
    private final UUID serviceId = UUID.randomUUID();
    private final String slug = "test-slug";

    private Specialist specialist;
    private Client client;
    private SpecialistSettings settings;
    private org.example.Service.Entity.Service service;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        specialist = new Specialist();
        specialist.setId(specialistId);
        specialist.setSlug(slug);
        specialist.setFullName("Test Specialist");

        client = new Client();
        client.setId(clientId);
        client.setName("Test Client");

        settings = new SpecialistSettings();
        settings.setMinBookingNoticeHours(2);
        settings.setMaxBookingDaysAhead(30);

        service = new org.example.Service.Entity.Service();
        service.setId(serviceId);
        service.setServiceName("Consultation");
        service.setDuration(60);
        service.setPrice(BigDecimal.valueOf(100));
        service.setActive(true);
        service.setSpecialist(specialist);

        settings.setDefaultService(service);

        appointment = Appointment.builder()
                .id(appointmentId)
                .client(client)
                .specialist(specialist)
                .service(service)
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .status(AppointmentStatus.PLANNED)
                .isPaid(false)
                .price(BigDecimal.valueOf(100))
                .duration(60)
                .source(BookingSource.CLIENT)
                .build();
    }

    // ---------- createAppointmentByClient & Internal ----------

    @Test
    void createAppointmentByClient_success() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                clientId, LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Note", serviceId
        );

        when(specialistRepository.findBySlug(slug)).thenReturn(Optional.of(specialist));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(specialistSettingRepository.findBySpecialist_Id(specialistId)).thenReturn(Optional.of(settings));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(availabilityService.availabilityForClient(any(), any(), any(), eq(specialistId), isNull())).thenReturn(true);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        ClientAppointmentResponse response = appointmentService.createAppointmentByClient(slug, request);

        assertNotNull(response);
        assertEquals(AppointmentStatus.PLANNED, response.status());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void createAppointmentByClient_exceedsMaxDaysAhead_throwsException() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                clientId, LocalDate.now().plusDays(40), LocalTime.of(10, 0), null, serviceId
        );

        when(specialistRepository.findBySlug(slug)).thenReturn(Optional.of(specialist));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(specialistSettingRepository.findBySpecialist_Id(specialistId)).thenReturn(Optional.of(settings));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));

        ApiException exception = assertThrows(ApiException.class, () ->
                appointmentService.createAppointmentByClient(slug, request)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void createAppointmentBySpecialist_success() {
        CreateAppointmentRequest request = new CreateAppointmentRequest(
                clientId, LocalDate.now().plusDays(1), LocalTime.of(10, 0), null, serviceId
        );

        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));
        when(specialistRepository.findBySlug(slug)).thenReturn(Optional.of(specialist));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(specialistSettingRepository.findBySpecialist_Id(specialistId)).thenReturn(Optional.of(settings));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(availabilityService.availabilityForSpecialist(any(), any(), any(), eq(specialistId), isNull())).thenReturn(true);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        SpecialistAppointmentDetails response = appointmentService.createAppointmentBySpecialist(specialistId, request);

        assertNotNull(response);
        assertEquals(AppointmentStatus.PLANNED, response.status());
    }

    // ---------- toggleCompleteAppointment ----------

    @Test
    void toggleCompleteAppointment_plannedToCompleted() {
        appointment.setStatus(AppointmentStatus.PLANNED);
        when(appointmentRepository.findByIdAndSpecialistId(appointmentId, specialistId)).thenReturn(Optional.of(appointment));

        SpecialistAppointmentDetails response = appointmentService.toggleCompleteAppointment(specialistId, appointmentId);

        assertEquals(AppointmentStatus.COMPLETED, response.status());
    }

    @Test
    void toggleCompleteAppointment_completedToPlanned() {
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findByIdAndSpecialistId(appointmentId, specialistId)).thenReturn(Optional.of(appointment));

        SpecialistAppointmentDetails response = appointmentService.toggleCompleteAppointment(specialistId, appointmentId);

        assertEquals(AppointmentStatus.PLANNED, response.status());
    }

    @Test
    void toggleCompleteAppointment_notFound_throwsException() {
        when(appointmentRepository.findByIdAndSpecialistId(appointmentId, specialistId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                appointmentService.toggleCompleteAppointment(specialistId, appointmentId)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    // ---------- attachBillToAppointment & togglePaid ----------

    @Test
    void attachBillToAppointment_success() {
        AttachBillToAppointmentRequest request = new AttachBillToAppointmentRequest("http://bill.url");
        when(appointmentRepository.findByIdAndSpecialistId(appointmentId, specialistId)).thenReturn(Optional.of(appointment));

        SpecialistAppointmentDetails response = appointmentService.attachBillToAppointment(specialistId, appointmentId, request);

        assertTrue(response.paid()); // Исправлено с isPaid() на paid()
        assertEquals("http://bill.url", response.receiptUrl());
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void togglePaid_unpaidToPaid() {
        appointment.setPaid(false);
        when(appointmentRepository.findByIdAndSpecialistId(appointmentId, specialistId)).thenReturn(Optional.of(appointment));

        SpecialistAppointmentDetails response = appointmentService.togglePaid(specialistId, appointmentId);

        assertTrue(response.paid()); // Исправлено с isPaid() на paid()
    }

    @Test
    void togglePaid_paidWithReceipt_throwsException() {
        appointment.setPaid(true);
        appointment.setReceiptUrl("http://bill.url");
        when(appointmentRepository.findByIdAndSpecialistId(appointmentId, specialistId)).thenReturn(Optional.of(appointment));

        ApiException exception = assertThrows(ApiException.class, () ->
                appointmentService.togglePaid(specialistId, appointmentId)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    // ---------- toggleCancelAppointmentBySpecialist ----------

    @Test
    void toggleCancelAppointmentBySpecialist_plannedToCancelled() {
        appointment.setStatus(AppointmentStatus.PLANNED);
        when(appointmentRepository.findByIdAndSpecialistId(appointmentId, specialistId)).thenReturn(Optional.of(appointment));

        SpecialistAppointmentDetails response = appointmentService.toggleCancelAppointmentBySpecialist(specialistId, appointmentId);

        assertEquals(AppointmentStatus.CANCELLED, response.status());
    }

    @Test
    void toggleCancelAppointmentBySpecialist_cancelledToPlanned_success() {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findByIdAndSpecialistId(appointmentId, specialistId)).thenReturn(Optional.of(appointment));
        when(availabilityService.availabilityForSpecialist(any(), any(), any(), eq(specialistId), eq(appointmentId))).thenReturn(true);

        SpecialistAppointmentDetails response = appointmentService.toggleCancelAppointmentBySpecialist(specialistId, appointmentId);

        assertEquals(AppointmentStatus.PLANNED, response.status());
    }

    @Test
    void toggleCancelAppointmentBySpecialist_cancelledToPlanned_slotTaken_throwsException() {
        appointment.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findByIdAndSpecialistId(appointmentId, specialistId)).thenReturn(Optional.of(appointment));
        when(availabilityService.availabilityForSpecialist(any(), any(), any(), eq(specialistId), eq(appointmentId))).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, () ->
                appointmentService.toggleCancelAppointmentBySpecialist(specialistId, appointmentId)
        );
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    // ---------- rescheduleAppointmentByClient ----------

    @Test
    void rescheduleAppointmentByClient_success() {
        RescheduleRequest request = new RescheduleRequest(
                appointmentId, LocalDate.now().plusDays(2), LocalTime.of(12, 0)
        );

        when(appointmentRepository.findByIdAndClientId(appointmentId, clientId)).thenReturn(Optional.of(appointment));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(specialistSettingRepository.findBySpecialist_Id(specialistId)).thenReturn(Optional.of(settings));
        when(availabilityService.availabilityForClient(any(), any(), any(), eq(specialistId), eq(appointmentId))).thenReturn(true);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        ClientAppointmentResponse response = appointmentService.rescheduleAppointmentByClient(clientId, request);

        assertNotNull(response);
        verify(appointmentRepository).save(appointment);
    }

    // ---------- updateAppointmentBySpecialist ----------

    @Test
    void updateAppointmentBySpecialist_updateOnlyNote_doesNotCallSaveExplicitly() {
        UpdateAppointmentRequest request = new UpdateAppointmentRequest("Updated note", null, null, null);

        when(appointmentRepository.findByIdAndSpecialistId(appointmentId, specialistId)).thenReturn(Optional.of(appointment));

        SpecialistAppointmentDetails response = appointmentService.updateAppointmentBySpecialist(specialistId, appointmentId, request);

        assertEquals("Updated note", response.note());
        // Проверяем, что save НЕ вызывался явно, так как сработал ранний return
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void updateAppointmentBySpecialist_updateTime_successAndSaves() {
        UpdateAppointmentRequest request = new UpdateAppointmentRequest(
                null, LocalDate.now().plusDays(2), LocalTime.of(12, 0), null
        );

        when(appointmentRepository.findByIdAndSpecialistId(appointmentId, specialistId)).thenReturn(Optional.of(appointment));
        when(availabilityService.availabilityForSpecialist(any(), any(), any(), eq(specialistId), eq(appointmentId))).thenReturn(true);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        SpecialistAppointmentDetails response = appointmentService.updateAppointmentBySpecialist(specialistId, appointmentId, request);

        assertNotNull(response);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    // ---------- cancelAppointmentByClient ----------

    @Test
    void cancelAppointmentByClient_success() {
        when(appointmentRepository.findByIdAndClientId(appointmentId, clientId)).thenReturn(Optional.of(appointment));
        when(specialistSettingRepository.findBySpecialist_Id(specialistId)).thenReturn(Optional.of(settings));

        ClientAppointmentResponse response = appointmentService.cancelAppointmentByClient(clientId, appointmentId);

        assertEquals(AppointmentStatus.CANCELLED, response.status());
    }

    @Test
    void cancelAppointmentByClient_tooLate_throwsException() {
        appointment.setDate(LocalDate.now());
        appointment.setStartTime(LocalTime.now().plusMinutes(30));

        when(appointmentRepository.findByIdAndClientId(appointmentId, clientId)).thenReturn(Optional.of(appointment));
        when(specialistSettingRepository.findBySpecialist_Id(specialistId)).thenReturn(Optional.of(settings));

        ApiException exception = assertThrows(ApiException.class, () ->
                appointmentService.cancelAppointmentByClient(clientId, appointmentId)
        );
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }
}