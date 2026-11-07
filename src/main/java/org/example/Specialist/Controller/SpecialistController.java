package org.example.Specialist.Controller;

import org.example.Security.UserPrincipal;
import org.example.Specialist.DTO.SettingResponse;
import org.example.Specialist.DTO.SpecialistResponse;
import org.example.Specialist.Entity.SpecialistSettings;
import org.example.Specialist.Service.SpecialistService;
import org.example.Specialist.Service.SpecialistSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/specialist")
public class SpecialistController {
    private final SpecialistService specialistService;
    private final SpecialistSettingService specialistSettingService;

    public SpecialistController(SpecialistService specialistService,
                                SpecialistSettingService specialistSettingService){
        this.specialistService = specialistService;
        this.specialistSettingService = specialistSettingService;
    }

    @GetMapping("/profile")
    public ResponseEntity<SpecialistResponse> getSpecialistProfile(@AuthenticationPrincipal UserPrincipal principal){
        return ResponseEntity.ok(specialistService.getSpecialistById(principal.getId()));
    }

    @GetMapping("/settings")
    public ResponseEntity<SettingResponse> getSpecialistSettings(@AuthenticationPrincipal UserPrincipal principal){
        return ResponseEntity.ok(specialistSettingService.getSettings(principal.getId()));
    }

    
}
