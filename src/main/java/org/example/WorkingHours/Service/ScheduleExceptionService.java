package org.example.WorkingHours.Service;

import org.example.Common.Exception.ApiException;
import org.example.Specialist.Entity.Specialist;
import org.example.Specialist.Repository.SpecialistRepository;
import org.example.WorkingHours.DTO.*;
import org.example.WorkingHours.Entity.ScheduleException;
import org.example.WorkingHours.Entity.WorkingHours;
import org.example.WorkingHours.Repository.ScheduleExceptionRepository;
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
public class ScheduleExceptionService {
    private final ScheduleExceptionRepository scheduleExceptionRepository;
    private final SpecialistRepository specialistRepository;

    public ScheduleExceptionService(ScheduleExceptionRepository scheduleExceptionRepository,
                               SpecialistRepository specialistRepository){
        this.scheduleExceptionRepository = scheduleExceptionRepository;
        this.specialistRepository = specialistRepository;
    }

    @Transactional
    public List<WorkingHoursResponse> createScheduleException(UUID specialistId, CreateScheduleExceptionRequest request){
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));


        return toScheduleExceptionResponse(scheduleException);
    }

    public List<WorkingHoursResponse> getAllScheduleExceptionByDay(UUID specialistId){
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        List<WorkingHours> workingHours = workingHoursRepository.findAllWorkingHoursBySpecialist_Id(specialist.getId());

        return workingHours.stream().map(this::toScheduleExceptionResponse).toList();
    }

    @Transactional
    public WorkingHoursResponse updateScheduleException(UUID specialistId, UUID workingHourId, UpdateWorkingHoursRequest request){
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

        workingHour.setDayOfWeek(targetDay);
        workingHour.setStartTime(targetStart);
        workingHour.setEndTime(targetEnd);

        workingHoursRepository.save(workingHour);
        return toScheduleExceptionResponse(workingHour);
    }

    @Transactional
    public void deleteScheduleException(UUID specialistId, UUID workingHoursId){
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
                                   List<ScheduleException> existingHours, UUID excludeWorkingHoursId){
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new ApiException("Start time must be before end time", HttpStatus.BAD_REQUEST);
        }

        for (ScheduleException existing : existingHours) {
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

    private ScheduleExceptionResponse toScheduleExceptionResponse(ScheduleException scheduleException){
        return new WorkingHoursResponse(workingHours.getId()
                ,workingHours.getSpecialist()
                ,workingHours.getDayOfWeek()
                ,workingHours.getStartTime()
                ,workingHours.getEndTime());
    }
}
