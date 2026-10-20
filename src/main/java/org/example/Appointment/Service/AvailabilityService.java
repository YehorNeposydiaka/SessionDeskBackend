package org.example.Appointment.Service;

import org.example.Appointment.Entity.Appointment;
import org.example.Appointment.Repository.AppointmentRepository;
import org.example.Specialist.Entity.Specialist;
import org.example.WorkingHours.Entity.ScheduleException;
import org.example.WorkingHours.Entity.WorkingHours;
import org.example.WorkingHours.Repository.ScheduleExceptionRepository;
import org.example.WorkingHours.Repository.WorkingHoursRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public boolean AvailabilityForClient(LocalDate date, LocalTime startTime, LocalTime endTime, UUID specialistId){
        boolean result = workingHoursCheck(date, startTime, endTime, specialistId)
                && appointmentCheck(date, startTime, endTime, specialistId)
                && scheduleExceptionCheck(date, startTime, endTime, specialistId);
        return result;
    }

    public boolean AvailabilityForSpecialist(LocalDate date, LocalTime startTime, LocalTime endTime, UUID  specialistId){
        boolean result = appointmentCheck(date, startTime, endTime, specialistId);
        return result;
    }

    private boolean workingHoursCheck(LocalDate date, LocalTime startTime, LocalTime endTime, UUID  specialistId){
        List<WorkingHours> workingHours = workingHoursRepository.findAllBySpecialistIdAndDayOfWeek(specialistId, date.getDayOfWeek());
        return true;
    }

    private boolean appointmentCheck(LocalDate date, LocalTime startTime, LocalTime endTime, UUID  specialistId){
        List<Appointment> existingAppointments = appointmentRepository.findAllBySpecialistIdAndDateBetween(specialistId, date, date);
        return true;
    }

    private boolean scheduleExceptionCheck(LocalDate date, LocalTime startTime, LocalTime endTime, UUID  specialistId){
        List<ScheduleException> exceptions = scheduleExceptionRepository.findAllBySpecialistIdAndDateBetween(specialistId, date, date);
        return true;
    }
}
