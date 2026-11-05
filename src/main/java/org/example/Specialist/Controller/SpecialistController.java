package org.example.Specialist.Controller;

import org.example.Specialist.Service.SpecialistService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/specialist")
public class SpecialistController {
    private final SpecialistService specialistService;

    public SpecialistController(SpecialistService specialistService){
        this.specialistService = specialistService;
    }


}
