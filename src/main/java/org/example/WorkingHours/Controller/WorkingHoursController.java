package org.example.WorkingHours.Controller;

import jakarta.validation.Valid;
import org.example.Security.UserPrincipal;
import org.example.WorkingHours.DTO.CreateWorkingHourRequest;
import org.example.WorkingHours.DTO.UpdateWorkingHoursRequest;
import org.example.WorkingHours.DTO.WorkingHoursResponse;
import org.example.WorkingHours.Service.ScheduleExceptionService;
import org.example.WorkingHours.Service.WorkingHoursService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
                                                   @Valid @RequestBody UpdateWorkingHoursRequest request,
                                                   @PathVariable UUID workingHourId) {
        WorkingHoursResponse response = workingHoursService.updateWorkingHours(principal.getId(), workingHourId, request);
        return ResponseEntity.noContent().build();
    }
}
