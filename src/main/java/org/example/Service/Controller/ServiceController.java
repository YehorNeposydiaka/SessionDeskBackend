package org.example.Service.Controller;

import jakarta.validation.Valid;
import org.example.Security.UserPrincipal;
import org.example.Service.DTO.CreateServiceRequest;
import org.example.Service.DTO.ServiceResponse;
import org.example.Service.DTO.UpdateServiceRequest;
import org.example.Service.Service.ServiceManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/services")
public class ServiceController {
    private final ServiceManagementService serviceManagementService;

    public ServiceController(ServiceManagementService serviceManagementService){
        this.serviceManagementService = serviceManagementService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getAllSpecialistServices(@AuthenticationPrincipal UserPrincipal principal){
        List<ServiceResponse> response = serviceManagementService.getAllServiceBySpecialist(principal.getId());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<List<ServiceResponse>> getServicesBySlug(@PathVariable String slug){
        List<ServiceResponse> response = serviceManagementService.getAllServiceBySlug(slug);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable UUID serviceId){
        ServiceResponse response = serviceManagementService.getServiceById(serviceId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> createService(@AuthenticationPrincipal UserPrincipal principal,
                                                         @Valid @RequestBody CreateServiceRequest request){
        ServiceResponse response = serviceManagementService.createService(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{serviceId}")
    public ResponseEntity<ServiceResponse> updateService(@AuthenticationPrincipal UserPrincipal principal,
                                                         @PathVariable UUID serviceId,
                                                         @Valid @RequestBody UpdateServiceRequest request){
        ServiceResponse response = serviceManagementService.updateService(serviceId, principal.getId(), request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{serviceId}/toggleActive")
    public ResponseEntity<Void> toggleActiveStatus(@AuthenticationPrincipal UserPrincipal principal,
                                                    @PathVariable UUID serviceId){
        serviceManagementService.changeActiveStatus(serviceId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
