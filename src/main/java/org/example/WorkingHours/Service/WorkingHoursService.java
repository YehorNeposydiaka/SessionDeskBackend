package org.example.WorkingHours.Service;

import org.example.Common.Exception.ApiException;
import org.example.Specialist.Entity.Specialist;
import org.example.Specialist.Repository.SpecialistRepository;
import org.example.WorkingHours.DTO.CreateWorkingHourRequest;
import org.example.WorkingHours.DTO.WorkingHoursResponse;
import org.example.WorkingHours.Entity.WorkingHours;
import org.example.WorkingHours.Repository.WorkingHoursRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class WorkingHoursService {
    private final WorkingHoursRepository workingHoursRepository;
    private final SpecialistRepository specialistRepository;

    public WorkingHoursService(WorkingHoursRepository workingHoursRepository,
                               SpecialistRepository specialistRepository){
        this.workingHoursRepository = workingHoursRepository;
        this.specialistRepository = specialistRepository;
    }

    @Transactional
    public List<WorkingHoursResponse> createWorkingHours(UUID specialistId, List<CreateWorkingHourRequest> requests){
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        List<WorkingHours> hours = new ArrayList<>();
        for(CreateWorkingHourRequest request : requests){
            hours.add(WorkingHours.builder().dayOfWeek(request.day())
                    .startTime(request.startTime())
                    .endTime(request.endTime())
                    .specialist(specialist).build());
        }

        List<WorkingHours> savedHours = workingHoursRepository.saveAll(hours);

        return hours.stream().map(this::toWorkingHoursResponse).toList();
    }


    private WorkingHoursResponse toWorkingHoursResponse(WorkingHours workingHours){
        return new WorkingHoursResponse(workingHours.getId()
                ,workingHours.getSpecialist()
                ,workingHours.getDayOfWeek()
                ,workingHours.getStartTime()
                ,workingHours.getEndTime());
    }
}
