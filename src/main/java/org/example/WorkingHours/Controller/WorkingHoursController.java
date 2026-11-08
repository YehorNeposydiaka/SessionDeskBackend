package org.example.WorkingHours.Controller;

import jakarta.validation.Valid;
import org.example.Security.UserPrincipal;
import org.example.WorkingHours.DTO.*;
import org.example.WorkingHours.Service.ScheduleExceptionService;
import org.example.WorkingHours.Service.WorkingHoursService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/specialist/working-hours")
public class WorkingHoursController {

    private final WorkingHoursService workingHoursService;
    private final ScheduleExceptionService scheduleExceptionService;

    public WorkingHoursController(WorkingHoursService workingHoursService,
                                  ScheduleExceptionService scheduleExceptionService) {
        this.workingHoursService = workingHoursService;
        this.scheduleExceptionService = scheduleExceptionService;
    }

    @PostMapping
    public ResponseEntity<List<WorkingHoursResponse>> createWorkingHours(@AuthenticationPrincipal UserPrincipal principal,
                                                                         @Valid @RequestBody List<CreateWorkingHourRequest> request) {
        List<WorkingHoursResponse> response = workingHoursService.createWorkingHours(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<WorkingHoursResponse>> getAllWorkingHours(@AuthenticationPrincipal UserPrincipal principal) {
        List<WorkingHoursResponse> response = workingHoursService.getAllWorkingHoursBySpecialist(principal.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{workingHourId}")
    public ResponseEntity<WorkingHoursResponse> updateWorkingHours(@AuthenticationPrincipal UserPrincipal principal,
                                                                   @Valid @RequestBody UpdateWorkingHoursRequest request,
                                                                   @PathVariable UUID workingHourId) {
        WorkingHoursResponse response = workingHoursService.updateWorkingHours(principal.getId(), workingHourId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{workingHourId}")
    public ResponseEntity<Void> deleteWorkingHours(@AuthenticationPrincipal UserPrincipal principal,
                                                   @PathVariable UUID workingHourId) {
        workingHoursService.deleteWorkingHours(principal.getId(), workingHourId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/exceptions")
    public ResponseEntity<ScheduleExceptionResponse> createScheduleException(@AuthenticationPrincipal UserPrincipal principal,
                                                                             @Valid @RequestBody CreateScheduleExceptionRequest request){
        ScheduleExceptionResponse response = scheduleExceptionService.createScheduleException(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/exceptions")
    public ResponseEntity<List<ScheduleExceptionResponse>> getAllExceptionsForPeriod(@AuthenticationPrincipal UserPrincipal principal,
                                                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate){
        List<ScheduleExceptionResponse> response = scheduleExceptionService.getSpecialistScheduleException(principal.getId(), startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/exceptions/{exceptionId}")
    public ResponseEntity<ScheduleExceptionResponse> updateScheduleException(@AuthenticationPrincipal UserPrincipal principal,
                                                                             @Valid @RequestBody UpdateScheduleExceptionRequest request,
                                                                             @PathVariable UUID exceptionId){
        ScheduleExceptionResponse response = scheduleExceptionService.updateScheduleException(principal.getId(), exceptionId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/exceptions/{exceptionId}")
    public ResponseEntity<Void> deleteScheduleException(@AuthenticationPrincipal UserPrincipal principal,
                                                        @PathVariable UUID exceptionId){
        scheduleExceptionService.deleteScheduleException(principal.getId(), exceptionId);
        return ResponseEntity.noContent().build();
    }
}
