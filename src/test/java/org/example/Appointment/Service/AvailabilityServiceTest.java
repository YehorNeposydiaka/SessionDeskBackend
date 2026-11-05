package org.example.Appointment.Service;

import org.example.Appointment.Entity.Appointment;
import org.example.Appointment.Entity.AppointmentStatus;
import org.example.Appointment.Repository.AppointmentRepository;
import org.example.WorkingHours.Entity.ScheduleException;
import org.example.WorkingHours.Entity.ScheduleExceptionType;
import org.example.WorkingHours.Entity.WorkingHours;
import org.example.WorkingHours.Repository.ScheduleExceptionRepository;
import org.example.WorkingHours.Repository.WorkingHoursRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private WorkingHoursRepository workingHoursRepository;
    @Mock
    private ScheduleExceptionRepository scheduleExceptionRepository;
    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private final UUID specialistId = UUID.randomUUID();
    private final LocalDate testDate = LocalDate.of(2026, 6, 6); // Суббота
    private final LocalTime startTime = LocalTime.of(10, 0);
    private final LocalTime endTime = LocalTime.of(11, 0);

    @BeforeEach
    void setUp() {
        WorkingHours wh = new WorkingHours();
        wh.setStartTime(LocalTime.of(9, 0));
        wh.setEndTime(LocalTime.of(18, 0));

        // Используем lenient() для дефолтных заглушек, чтобы Mockito не ругался в тестах, где они не задействованы
        lenient().when(workingHoursRepository.findAllBySpecialistIdAndDayOfWeek(eq(specialistId), any(DayOfWeek.class)))
                .thenReturn(List.of(wh));

        lenient().when(appointmentRepository.findAllBySpecialistIdAndDateBetween(eq(specialistId), any(), any()))
                .thenReturn(Collections.emptyList());

        lenient().when(scheduleExceptionRepository.findAllBySpecialistIdAndDateBetween(eq(specialistId), any(), any()))
                .thenReturn(Collections.emptyList());
    }

    // ---------- availabilityForClient: позитивные сценарии ----------

    @Test
    void availabilityForClient_shouldReturnTrue_whenSlotIsCompletelyFree() {
        boolean result = availabilityService.availabilityForClient(
                testDate, startTime, endTime, specialistId, null
        );

        assertTrue(result);
    }

    // ---------- availabilityForClient: проверка рабочих часов (workingHoursCheck) ----------

    @Test
    void availabilityForClient_shouldReturnFalse_whenSpecialistDoesNotWorkOnThisDay() {
        when(workingHoursRepository.findAllBySpecialistIdAndDayOfWeek(eq(specialistId), any(DayOfWeek.class)))
                .thenReturn(Collections.emptyList());

        boolean result = availabilityService.availabilityForClient(
                testDate, startTime, endTime, specialistId, null
        );

        assertFalse(result);
    }

    @Test
    void availabilityForClient_shouldReturnFalse_whenTimeIsOutsideWorkingHours() {
        // Рабочие часы с 12:00 до 15:00, а мы запрашиваем с 10:00 до 11:00
        WorkingHours wh = new WorkingHours();
        wh.setStartTime(LocalTime.of(12, 0));
        wh.setEndTime(LocalTime.of(15, 0));
        when(workingHoursRepository.findAllBySpecialistIdAndDayOfWeek(eq(specialistId), any(DayOfWeek.class)))
                .thenReturn(List.of(wh));

        boolean result = availabilityService.availabilityForClient(
                testDate, startTime, endTime, specialistId, null
        );

        assertFalse(result);
    }

    // ---------- availabilityForClient: проверка записей (appointmentCheck) ----------

    @Test
    void availabilityForClient_shouldReturnFalse_whenOverlapsWithExistingActiveAppointment() {
        Appointment conflictingAppointment = new Appointment();
        conflictingAppointment.setId(UUID.randomUUID());
        conflictingAppointment.setStatus(AppointmentStatus.PLANNED);
        conflictingAppointment.setStartTime(LocalTime.of(10, 30));
        conflictingAppointment.setEndTime(LocalTime.of(11, 30));

        when(appointmentRepository.findAllBySpecialistIdAndDateBetween(eq(specialistId), any(), any()))
                .thenReturn(List.of(conflictingAppointment));

        boolean result = availabilityService.availabilityForClient(
                testDate, startTime, endTime, specialistId, null
        );

        assertFalse(result);
    }

    @Test
    void availabilityForClient_shouldReturnTrue_whenOverlapsWithCancelledAppointment() {
        Appointment cancelledAppointment = new Appointment();
        cancelledAppointment.setId(UUID.randomUUID());
        cancelledAppointment.setStatus(AppointmentStatus.CANCELLED);
        cancelledAppointment.setStartTime(LocalTime.of(10, 30));
        cancelledAppointment.setEndTime(LocalTime.of(11, 30));

        when(appointmentRepository.findAllBySpecialistIdAndDateBetween(eq(specialistId), any(), any()))
                .thenReturn(List.of(cancelledAppointment));

        boolean result = availabilityService.availabilityForClient(
                testDate, startTime, endTime, specialistId, null
        );

        assertTrue(result);
    }

    @Test
    void availabilityForClient_shouldReturnTrue_whenOverlapsWithItselfByExceptionId() {
        UUID currentAppointmentId = UUID.randomUUID();

        Appointment currentAppointment = new Appointment();
        currentAppointment.setId(currentAppointmentId);
        currentAppointment.setStatus(AppointmentStatus.PLANNED);
        currentAppointment.setStartTime(LocalTime.of(10, 0));
        currentAppointment.setEndTime(LocalTime.of(11, 0));

        when(appointmentRepository.findAllBySpecialistIdAndDateBetween(eq(specialistId), any(), any()))
                .thenReturn(List.of(currentAppointment));

        boolean result = availabilityService.availabilityForClient(
                testDate, startTime, endTime, specialistId, currentAppointmentId
        );

        assertTrue(result);
    }

    // ---------- availabilityForClient: проверка исключений расписания (scheduleExceptionCheck) ----------

    @Test
    void availabilityForClient_shouldReturnFalse_whenOverlapsWithBlockedScheduleException() {
        ScheduleException exception = new ScheduleException();
        exception.setType(ScheduleExceptionType.BLOCKED);
        exception.setStartTime(LocalTime.of(9, 30));
        exception.setEndTime(LocalTime.of(10, 30));

        when(scheduleExceptionRepository.findAllBySpecialistIdAndDateBetween(eq(specialistId), any(), any()))
                .thenReturn(List.of(exception));

        boolean result = availabilityService.availabilityForClient(
                testDate, startTime, endTime, specialistId, null
        );

        assertFalse(result);
    }

    // ---------- availabilityForSpecialist: проверка метода ----------

    @Test
    void availabilityForSpecialist_shouldReturnTrue_whenNoConflictingAppointments() {
        boolean result = availabilityService.availabilityForSpecialist(
                testDate, startTime, endTime, specialistId, null
        );

        assertTrue(result);
    }

    @Test
    void availabilityForSpecialist_shouldReturnFalse_whenThereIsConflictingAppointment() {
        Appointment conflict = new Appointment();
        conflict.setId(UUID.randomUUID());
        conflict.setStatus(AppointmentStatus.PLANNED);
        conflict.setStartTime(LocalTime.of(9, 30));
        conflict.setEndTime(LocalTime.of(10, 30));

        when(appointmentRepository.findAllBySpecialistIdAndDateBetween(eq(specialistId), any(), any()))
                .thenReturn(List.of(conflict));

        boolean result = availabilityService.availabilityForSpecialist(
                testDate, startTime, endTime, specialistId, null
        );

        assertFalse(result);
    }
}