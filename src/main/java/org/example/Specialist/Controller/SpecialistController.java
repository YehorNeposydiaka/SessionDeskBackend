package org.example.Specialist.Controller;

import jakarta.validation.Valid;
import org.example.Security.UserPrincipal;
import org.example.Specialist.DTO.*;
import org.example.Specialist.Service.SpecialistService;
import org.example.Specialist.Service.SpecialistSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/specialist")
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
    @PatchMapping("/profile")
    public ResponseEntity<SpecialistResponse> updateSpecialistProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                                      @Valid @RequestBody UpdateSpecialistRequest request){
        return ResponseEntity.ok(specialistService.updateSpecialist(principal.getId(), request));
    }
    @PatchMapping("/profile/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                               @Valid @RequestBody ChangePasswordRequest request){
        specialistService.changeSpecialistPassword(principal.getId(), request);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/settings")
    public ResponseEntity<SettingResponse> getSpecialistSettings(@AuthenticationPrincipal UserPrincipal principal){
        return ResponseEntity.ok(specialistSettingService.getSettings(principal.getId()));
    }

    @PostMapping("/settings")
    public ResponseEntity<SettingResponse> createSpecialistSettings(@AuthenticationPrincipal UserPrincipal principal,
                                                                    @Valid @RequestBody CreateSettingsRequest request){
        return ResponseEntity.ok(specialistSettingService.createSettings(principal.getId(), request));
    }

    @PatchMapping("/settings")
    public ResponseEntity<SettingResponse> updateSpecialistSettings(@AuthenticationPrincipal UserPrincipal principal,
                                                                    @Valid @RequestBody UpdateSettingRequest request){
        return ResponseEntity.ok(specialistSettingService.updateSettings(principal.getId(), request));
    }

    //TODO: Implement specialist card by slug
}
