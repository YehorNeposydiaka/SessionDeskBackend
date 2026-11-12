package org.example.Appointment.Service;

import org.example.Appointment.DTO.AvailableSlotsResponse;
import org.example.Appointment.Entity.Appointment;
import org.example.Appointment.Entity.AppointmentStatus;
import org.example.Appointment.Repository.AppointmentRepository;
import org.example.Common.Exception.ApiException;
import org.example.Specialist.Entity.Specialist;
import org.example.Appointment.DTO.*;
import org.example.Specialist.Entity.SpecialistSettings;
import org.example.Specialist.Repository.SpecialistRepository;
import org.example.Specialist.Repository.SpecialistSettingRepository;
import org.example.WorkingHours.Entity.ScheduleException;
import org.example.WorkingHours.Entity.ScheduleExceptionType;
import org.example.WorkingHours.Entity.WorkingHours;
import org.example.WorkingHours.Repository.ScheduleExceptionRepository;
import org.example.WorkingHours.Repository.WorkingHoursRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

@Service
public class AvailabilityService {
    private final WorkingHoursRepository workingHoursRepository;
    private final ScheduleExceptionRepository scheduleExceptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final SpecialistRepository specialistRepository;
    private final SpecialistSettingRepository specialistSettingRepository;

    private static final int SLOT_STEP_MINUTES = 30;

    public AvailabilityService(WorkingHoursRepository workingHoursRepository,
                               ScheduleExceptionRepository scheduleExceptionRepository,
                               AppointmentRepository appointmentRepository,
                               SpecialistRepository specialistRepository,
                               SpecialistSettingRepository specialistSettingRepository){
        this.workingHoursRepository = workingHoursRepository;
        this.scheduleExceptionRepository = scheduleExceptionRepository;
        this.appointmentRepository = appointmentRepository;
        this.specialistRepository = specialistRepository;
        this.specialistSettingRepository = specialistSettingRepository;
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
    @Transactional(readOnly = true)
    public AvailableSlotsResponse getAvailableSlotsForDay(String slug, LocalDate date) {
        Specialist specialist = specialistRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        SpecialistSettings setting = specialistSettingRepository.findBySpecialist_Id(specialist.getId())
                .orElseThrow(() -> new ApiException("Settings not found", HttpStatus.NOT_FOUND));

        LocalDate today = LocalDate.now();

        if (date.isBefore(today))
            throw new ApiException("You cannot get available slots for past date", HttpStatus.CONFLICT);

        if (ChronoUnit.DAYS.between(today, date) > setting.getMaxBookingDaysAhead())
            throw new ApiException("Not available check available slots for this date", HttpStatus.FORBIDDEN);

        org.example.Service.Entity.Service service = setting.getDefaultService();
        if (service == null || !service.isActive())
            throw new ApiException("Service is not available", HttpStatus.CONFLICT);

        long duration = service.getDuration();
        if (duration <= 0)
            throw new ApiException("Service duration must be positive", HttpStatus.CONFLICT);

        LocalTime minStart = date.equals(today) ? LocalTime.now() : LocalTime.MIN;

        UUID specialistId = specialist.getId();

        List<WorkingHours> windows = workingHoursRepository
                .findAllBySpecialistIdAndDayOfWeek(specialistId, date.getDayOfWeek());

        List<ScheduleException> blocked = scheduleExceptionRepository
                .findAllBySpecialistIdAndDateBetween(specialistId, date, date).stream()
                .filter(e -> e.getType() == ScheduleExceptionType.BLOCKED)
                .toList();

        List<Appointment> busy = appointmentRepository
                .findAllBySpecialistIdAndDateBetween(specialistId, date, date).stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .toList();

        TreeSet<LocalTime> slots = new TreeSet<>();

        for (WorkingHours window : windows) {
            LocalTime start = window.getStartTime();
            while (true) {
                LocalTime end = start.plusMinutes(duration);
                if (!end.isAfter(start) || end.isAfter(window.getEndTime()))
                    break;

                if (!start.isBefore(minStart) && noOverlap(start, end, blocked, busy))
                    slots.add(start);

                LocalTime next = start.plusMinutes(SLOT_STEP_MINUTES);
                if (!next.isAfter(start))
                    break;
                start = next;
            }
        }

        return new AvailableSlotsResponse(date, slots.stream().map(SlotResponse::new).toList());
    }

    private boolean noOverlap(LocalTime start, LocalTime end,
                              List<ScheduleException> blocked, List<Appointment> busy) {
        for (ScheduleException e : blocked)
            if (start.isBefore(e.getEndTime()) && end.isAfter(e.getStartTime()))
                return false;
        for (Appointment a : busy)
            if (start.isBefore(a.getEndTime()) && end.isAfter(a.getStartTime()))
                return false;
        return true;
    }
}
