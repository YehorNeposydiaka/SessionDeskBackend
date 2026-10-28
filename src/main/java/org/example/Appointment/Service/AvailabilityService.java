package org.example.Appointment.Service;

import org.example.Appointment.Entity.Appointment;
import org.example.Appointment.Entity.AppointmentStatus;
import org.example.Appointment.Repository.AppointmentRepository;
import org.example.WorkingHours.Entity.ScheduleException;
import org.example.WorkingHours.Entity.ScheduleExceptionType;
import org.example.WorkingHours.Entity.WorkingHours;
import org.example.WorkingHours.Repository.ScheduleExceptionRepository;
import org.example.WorkingHours.Repository.WorkingHoursRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class AvailabilityService {
    private final WorkingHoursRepository workingHoursRepository;
    private final ScheduleExceptionRepository scheduleExceptionRepository;
    private final AppointmentRepository appointmentRepository;

    public AvailabilityService(WorkingHoursRepository workingHoursRepository,
                               ScheduleExceptionRepository scheduleExceptionRepository,
                               AppointmentRepository appointmentRepository){
        this.workingHoursRepository = workingHoursRepository;
        this.scheduleExceptionRepository = scheduleExceptionRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public boolean availabilityForClient(LocalDate date, LocalTime startTime, LocalTime endTime, UUID specialistId, UUID exceptionId){
        return workingHoursCheck(date, startTime, endTime, specialistId)
                && appointmentCheck(date, startTime, endTime, specialistId, exceptionId)
                && scheduleExceptionCheck(date, startTime, endTime, specialistId);
    }

    public boolean availabilityForSpecialist(LocalDate date, LocalTime startTime, LocalTime endTime, UUID  specialistId, UUID exceptionId){
        return appointmentCheck(date, startTime, endTime, specialistId, exceptionId);
    }

    private boolean workingHoursCheck(LocalDate date, LocalTime startTime, LocalTime endTime, UUID  specialistId){
        List<WorkingHours> workingHours = workingHoursRepository.findAllBySpecialistIdAndDayOfWeek(specialistId, date.getDayOfWeek());

        if(workingHours.isEmpty())
            return false;

        for(WorkingHours hours : workingHours){
            boolean isAfterOrEqualsStart = !startTime.isBefore(hours.getStartTime());
            boolean isBeforeOrEqualsEnd = !endTime.isAfter(hours.getEndTime());

            if(isAfterOrEqualsStart && isBeforeOrEqualsEnd) {
                return true;
            }
        }
        return false;
    }

    private boolean appointmentCheck(LocalDate date, LocalTime startTime, LocalTime endTime, UUID  specialistId, UUID exceptionId){
        List<Appointment> existing = appointmentRepository
                .findAllBySpecialistIdAndDateBetween(specialistId, date, date);

        for (Appointment a : existing) {
            if (a.getId().equals(exceptionId)) continue;
            if (a.getStatus() != AppointmentStatus.CANCELLED
                    && a.getStartTime().isBefore(endTime)
                    && a.getEndTime().isAfter(startTime))
                return false;
        }
        return true;
    }

    private boolean scheduleExceptionCheck(LocalDate date, LocalTime startTime, LocalTime endTime, UUID  specialistId){
        List<ScheduleException> allExceptions = scheduleExceptionRepository.findAllBySpecialistIdAndDateBetween(specialistId, date, date);

        List<ScheduleException> exceptions = allExceptions.stream()
                .filter(e -> e.getType() == ScheduleExceptionType.BLOCKED)
                .toList();

        if (exceptions.isEmpty()) {
            return true;
        }

        for(ScheduleException exception : exceptions){
            if(startTime.isBefore(exception.getEndTime()) && endTime.isAfter(exception.getStartTime()))
                return false;
        }
        return true;
    }
}
