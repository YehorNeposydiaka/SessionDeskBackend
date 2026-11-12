package org.example.Appointment.Controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.example.Appointment.DTO.*;
import org.example.Appointment.Entity.ClientActivityProjection;
import org.example.Appointment.Service.AppointmentService;
import org.example.Appointment.Service.AvailabilityService;
import org.example.Security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final AvailabilityService availabilityService;

    public AppointmentController(AppointmentService appointmentService,
                                 AvailabilityService availabilityService){
        this.appointmentService = appointmentService;
        this.availabilityService = availabilityService;
    }

    @PostMapping
    public ResponseEntity<SpecialistAppointmentDetails> createAppointmentBySpecialist(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateAppointmentRequest request){
        SpecialistAppointmentDetails response =
                appointmentService.createAppointmentBySpecialist(principal.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SpecialistAppointmentSummary>> getAppointmentsByPeriod(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to){
        List<SpecialistAppointmentSummary> response =
                appointmentService.getAllAppointmentsForSpecialist(principal.getId(), from, to);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<SpecialistAppointmentDetails> getAppointmentDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID appointmentId){
        SpecialistAppointmentDetails response =
                appointmentService.getAppointmentDetails(principal.getId(), appointmentId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{appointmentId}")
    public ResponseEntity<SpecialistAppointmentDetails> updateAppointment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID appointmentId,
            @Valid @RequestBody UpdateAppointmentRequest request){
        SpecialistAppointmentDetails response =
                appointmentService.updateAppointmentBySpecialist(principal.getId(), appointmentId, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{appointmentId}/toggleComplete")
    public ResponseEntity<SpecialistAppointmentDetails> toggleCompleteAppointment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID appointmentId){
        SpecialistAppointmentDetails response =
                appointmentService.toggleCompleteAppointment(principal.getId(), appointmentId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{appointmentId}/toggleCancel")
    public ResponseEntity<SpecialistAppointmentDetails> toggleCancelAppointment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID appointmentId){
        SpecialistAppointmentDetails response =
                appointmentService.toggleCancelAppointmentBySpecialist(principal.getId(), appointmentId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{appointmentId}/togglePaid")
    public ResponseEntity<SpecialistAppointmentDetails> togglePaid(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID appointmentId){
        SpecialistAppointmentDetails response =
                appointmentService.togglePaid(principal.getId(), appointmentId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{appointmentId}/bill")
    public ResponseEntity<SpecialistAppointmentDetails> attachBill(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID appointmentId,
            @Valid @RequestBody AttachBillToAppointmentRequest request){
        SpecialistAppointmentDetails response =
                appointmentService.attachBillToAppointment(principal.getId(), appointmentId, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/clients/{clientId}/activities")
    public ResponseEntity<Page<ClientActivityProjection>> getClientActivities(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID clientId,
            @RequestParam(defaultValue = "0") @Min(0) int page){
        Page<ClientActivityProjection> response =
                appointmentService.getClientAppointmentsAndNotes(principal.getId(), clientId, page);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{slug}/slots")
    public ResponseEntity<AvailableSlotsResponse> getAvailableSlots(
            @PathVariable String slug,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        AvailableSlotsResponse response = availabilityService.getAvailableSlotsForDay(slug, date);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{slug}/appointments")
    public ResponseEntity<ClientAppointmentResponse> createAppointmentByClient(
            @PathVariable String slug,
            @Valid @RequestBody CreateAppointmentRequest request){
        ClientAppointmentResponse response = appointmentService.createAppointmentByClient(slug, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // TODO: clientId має братись з токена клієнта або одноразового посилання, а не з параметра
    @PatchMapping("/appointments/reschedule")
    public ResponseEntity<ClientAppointmentResponse> rescheduleAppointmentByClient(
            @RequestParam UUID clientId,
            @Valid @RequestBody RescheduleRequest request){
        ClientAppointmentResponse response =
                appointmentService.rescheduleAppointmentByClient(clientId, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // TODO: так само, clientId тимчасово параметром
    @PatchMapping("/public/appointments/{appointmentId}/cancel")
    public ResponseEntity<ClientAppointmentResponse> cancelAppointmentByClient(
            @RequestParam UUID clientId,
            @PathVariable UUID appointmentId){
        ClientAppointmentResponse response =
                appointmentService.cancelAppointmentByClient(clientId, appointmentId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
