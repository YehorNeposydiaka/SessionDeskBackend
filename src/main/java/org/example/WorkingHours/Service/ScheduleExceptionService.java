package org.example.WorkingHours.Service;

import org.example.Common.Exception.ApiException;
import org.example.Specialist.Entity.Specialist;
import org.example.Specialist.Repository.SpecialistRepository;
import org.example.WorkingHours.DTO.*;
import org.example.WorkingHours.Entity.ScheduleException;
import org.example.WorkingHours.Repository.ScheduleExceptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
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
    public ScheduleExceptionResponse createScheduleException(UUID specialistId, CreateScheduleExceptionRequest request){
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));

        List<ScheduleException> exceptions = scheduleExceptionRepository.findAllBySpecialistIdAndDate(specialist.getId(), request.date());

        ScheduleException exception = ScheduleException.builder().date(request.date())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .specialist(specialist)
                .type(request.type()).build();

        validateNoOverlap(exception.getDate(), exception.getStartTime(), exception.getEndTime(), exceptions, null);

        scheduleExceptionRepository.save(exception);
        return toScheduleExceptionResponse(exception);
    }

    public List<ScheduleExceptionResponse> getSpecialistScheduleException(UUID specialistId, LocalDate startDate, LocalDate endDate){
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        if(startDate.isAfter(endDate)){
            throw new ApiException("Invalid time range", HttpStatus.BAD_REQUEST);
        }
        if(ChronoUnit.DAYS.between(startDate, endDate) >= 31){
            throw new ApiException("Choose period between 1 to 31 day", HttpStatus.BAD_REQUEST);
        }
        List<ScheduleException> exceptions = scheduleExceptionRepository.findAllBySpecialistIdAndDateBetween(specialist.getId(), startDate, endDate);

        return exceptions.stream().map(this::toScheduleExceptionResponse).toList();
    }

    @Transactional
    public ScheduleExceptionResponse updateScheduleException(UUID specialistId, UUID exceptionId, UpdateScheduleExceptionRequest request){
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        ScheduleException exception = scheduleExceptionRepository.findById(exceptionId)
                .orElseThrow(() -> new ApiException("Working hours not found", HttpStatus.NOT_FOUND));
        if(!exception.getSpecialist().getId().equals(specialist.getId())){
            throw new ApiException("Specialist can manage only its own schedule exceptions",HttpStatus.FORBIDDEN);
        }

        LocalDate targetDay = exception.getDate();
        LocalTime targetStart = request.startTime() != null ? request.startTime() : exception.getStartTime();
        LocalTime targetEnd = request.endTime() != null ? request.endTime() : exception.getEndTime();

        List<ScheduleException> exceptions = scheduleExceptionRepository.findAllBySpecialistIdAndDateBetween(specialist.getId(), targetDay, targetDay);

        validateNoOverlap(targetDay, targetStart, targetEnd, exceptions, exception.getId());

        exception.setStartTime(targetStart);
        exception.setEndTime(targetEnd);
        if(request.type() != null) exception.setType(request.type());

        scheduleExceptionRepository.save(exception);
        return toScheduleExceptionResponse(exception);
    }

    @Transactional
    public void deleteScheduleException(UUID specialistId, UUID exceptionId){
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        ScheduleException exception = scheduleExceptionRepository.findById(exceptionId)
                .orElseThrow(() -> new ApiException("Schedule exception not found", HttpStatus.NOT_FOUND));
        if(!exception.getSpecialist().getId().equals(specialist.getId())){
            throw new ApiException("Specialist can manage only its own schedule exceptions",HttpStatus.FORBIDDEN);
        }

        scheduleExceptionRepository.delete(exception);
    }

    private void validateNoOverlap(LocalDate date, LocalTime startTime, LocalTime endTime,
                                   List<ScheduleException> existingExceptions, UUID excludeExceptionId){
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new ApiException("Start time must be before end time", HttpStatus.BAD_REQUEST);
        }

        for (ScheduleException existing : existingExceptions) {
            if (excludeExceptionId != null && existing.getId().equals(excludeExceptionId)) {
                continue;
            }

            if (existing.getDate().equals(date)) {
                boolean isOverlapping = startTime.isBefore(existing.getEndTime())
                        && endTime.isAfter(existing.getStartTime());
                if (isOverlapping) {
                    throw new ApiException("Working hours overlap with existing schedule on " + date, HttpStatus.CONFLICT);
                }
            }
        }

    }

    private ScheduleExceptionResponse toScheduleExceptionResponse(ScheduleException scheduleException){
        return new ScheduleExceptionResponse(scheduleException.getId()
                ,scheduleException.getSpecialist().getId()
                ,scheduleException.getDate()
                ,scheduleException.getStartTime()
                ,scheduleException.getEndTime()
                , scheduleException.getType());
    }
}
