package org.example.WorkingHours.Service;

import org.example.Common.Exception.ApiException;
import org.example.Specialist.Entity.Specialist;
import org.example.Specialist.Repository.SpecialistRepository;
import org.example.WorkingHours.DTO.CreateWorkingHourRequest;
import org.example.WorkingHours.DTO.UpdateWorkingHoursRequest;
import org.example.WorkingHours.DTO.WorkingHoursResponse;
import org.example.WorkingHours.Entity.WorkingHours;
import org.example.WorkingHours.Repository.WorkingHoursRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
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

        List<WorkingHours> existingHours = workingHoursRepository.findAllWorkingHoursBySpecialist_Id(specialist.getId());
        List<WorkingHours> hours = new ArrayList<>();

        for(CreateWorkingHourRequest request : requests){

            List<WorkingHours> allToCheck = new ArrayList<>(existingHours);
            allToCheck.addAll(hours);

            validateNoOverlap(request.day(), request.startTime(), request.endTime(), allToCheck, null);
            hours.add(WorkingHours.builder().dayOfWeek(request.day())
                    .startTime(request.startTime())
                    .endTime(request.endTime())
                    .specialist(specialist).build());
        }

        workingHoursRepository.saveAll(hours);

        return hours.stream().map(this::toWorkingHoursResponse).toList();
    }

    public List<WorkingHoursResponse> getALLWorkingHoursBySpecialist(UUID specialistId){
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        List<WorkingHours> workingHours = workingHoursRepository.findAllWorkingHoursBySpecialist_Id(specialist.getId());

        return workingHours.stream().map(this::toWorkingHoursResponse).toList();
    }

    @Transactional
    public WorkingHoursResponse updateWorkingHours(UUID specialistId, UUID workingHourId, UpdateWorkingHoursRequest request){
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        WorkingHours workingHour = workingHoursRepository.findById(workingHourId)
                .orElseThrow(() -> new ApiException("Working hours not found", HttpStatus.NOT_FOUND));
        if(!workingHour.getSpecialist().getId().equals(specialist.getId())){
            throw new ApiException("Specialist can manage only its own working hours",HttpStatus.FORBIDDEN);
        }

        DayOfWeek targetDay = workingHour.getDayOfWeek();
        LocalTime targetStart = request.startTime() != null ? request.startTime() : workingHour.getStartTime();
        LocalTime targetEnd = request.endTime() != null ? request.endTime() : workingHour.getEndTime();

        List<WorkingHours> existingHours = workingHoursRepository.findAllWorkingHoursBySpecialist_Id(specialistId);

        validateNoOverlap(targetDay, targetStart, targetEnd, existingHours, workingHourId);

        workingHour.setDayOfWeek(targetDay);
        workingHour.setStartTime(targetStart);
        workingHour.setEndTime(targetEnd);

        workingHoursRepository.save(workingHour);
        return toWorkingHoursResponse(workingHour);
    }

    @Transactional
    public void deleteWorkingHours(UUID specialistId, UUID workingHoursId){
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        WorkingHours workingHours = workingHoursRepository.findById(workingHoursId)
                .orElseThrow(() -> new ApiException("Working hours not found", HttpStatus.NOT_FOUND));
        if(!workingHours.getSpecialist().getId().equals(specialist.getId())){
            throw new ApiException("Specialist can manage only its own working hours",HttpStatus.FORBIDDEN);
        }

        workingHoursRepository.delete(workingHours);
    }

    private void validateNoOverlap(DayOfWeek day, LocalTime startTime, LocalTime endTime,
                                   List<WorkingHours> existingHours, UUID excludeWorkingHoursId){
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new ApiException("Start time must be before end time", HttpStatus.BAD_REQUEST);
        }

        for (WorkingHours existing : existingHours) {
            if (excludeWorkingHoursId != null && existing.getId().equals(excludeWorkingHoursId)) {
                continue;
            }

            if (existing.getDayOfWeek().equals(day)) {
                boolean isOverlapping = startTime.isBefore(existing.getEndTime())
                        && endTime.isAfter(existing.getStartTime());
                if (isOverlapping) {
                    throw new ApiException("Working hours overlap with existing schedule on " + day, HttpStatus.CONFLICT);
                }
            }
        }

    }
    private WorkingHoursResponse toWorkingHoursResponse(WorkingHours workingHours){
        return new WorkingHoursResponse(workingHours.getId()
                ,workingHours.getSpecialist().getId()
                ,workingHours.getDayOfWeek()
                ,workingHours.getStartTime()
                ,workingHours.getEndTime());
    }
}
