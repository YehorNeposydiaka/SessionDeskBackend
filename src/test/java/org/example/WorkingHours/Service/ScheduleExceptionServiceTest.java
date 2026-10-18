package org.example.WorkingHours.Service;

import org.example.Common.Exception.ApiException;
import org.example.Specialist.Entity.Specialist;
import org.example.Specialist.Repository.SpecialistRepository;
import org.example.WorkingHours.DTO.*;
import org.example.WorkingHours.Entity.ScheduleException;
import org.example.WorkingHours.Entity.ScheduleExceptionType; // Замініть на ваш реальний імпорт типу, якщо потрібно
import org.example.WorkingHours.Repository.ScheduleExceptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleExceptionServiceTest {

    @Mock
    private ScheduleExceptionRepository scheduleExceptionRepository;

    @Mock
    private SpecialistRepository specialistRepository;

    @InjectMocks
    private ScheduleExceptionService scheduleExceptionService;

    private UUID specialistId;
    private UUID exceptionId;
    private Specialist specialist;
    private ScheduleException scheduleException;

    @BeforeEach
    void setUp() {
        specialistId = UUID.randomUUID();
        exceptionId = UUID.randomUUID();

        specialist = new Specialist();
        specialist.setId(specialistId);

        scheduleException = ScheduleException.builder()
                .id(exceptionId)
                .specialist(specialist)
                .date(LocalDate.of(2026, 6, 10))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .type(ScheduleExceptionType.BLOCKED)
                .build();
    }

    // --- Тести для createScheduleException ---

    @Test
    void createScheduleException_Success() {
        CreateScheduleExceptionRequest request = new CreateScheduleExceptionRequest(
                LocalDate.of(2026, 6, 10),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                ScheduleExceptionType.BLOCKED
        );

        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));
        when(scheduleExceptionRepository.findAllBySpecialistIdAndDate(specialistId, request.date()))
                .thenReturn(Collections.emptyList());
        when(scheduleExceptionRepository.save(any(ScheduleException.class))).thenAnswer(invocation -> {
            ScheduleException savedEntity = invocation.getArgument(0);
            savedEntity.setId(exceptionId);
            return savedEntity;
        });

        ScheduleExceptionResponse response = scheduleExceptionService.createScheduleException(specialistId, request);

        assertNotNull(response);
        assertEquals(exceptionId, response.id());
        verify(scheduleExceptionRepository, times(1)).save(any(ScheduleException.class));
    }

    @Test
    void createScheduleException_SpecialistNotFound() {
        CreateScheduleExceptionRequest request = new CreateScheduleExceptionRequest(
                LocalDate.of(2026, 6, 10), LocalTime.of(9, 0), LocalTime.of(17, 0), ScheduleExceptionType.BLOCKED
        );

        when(specialistRepository.findById(specialistId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                scheduleExceptionService.createScheduleException(specialistId, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Specialist not found", exception.getMessage());
        verify(scheduleExceptionRepository, never()).save(any());
    }

    // --- Тести для getSpecialistScheduleException ---

    @Test
    void getSpecialistScheduleException_Success() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 10);

        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));
        when(scheduleExceptionRepository.findAllBySpecialistIdAndDateBetween(specialistId, start, end))
                .thenReturn(List.of(scheduleException));

        List<ScheduleExceptionResponse> responses = scheduleExceptionService.getSpecialistScheduleException(specialistId, start, end);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(exceptionId, responses.get(0).id());
    }

    @Test
    void getSpecialistScheduleException_InvalidTimeRange() {
        LocalDate start = LocalDate.of(2026, 6, 10);
        LocalDate end = LocalDate.of(2026, 6, 1); // Початок пізніше за кінець

        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));

        ApiException exception = assertThrows(ApiException.class, () ->
                scheduleExceptionService.getSpecialistScheduleException(specialistId, start, end)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Invalid time range", exception.getMessage());
    }

    @Test
    void getSpecialistScheduleException_PeriodTooLong() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 7, 15); // Більше 31 дня

        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));

        ApiException exception = assertThrows(ApiException.class, () ->
                scheduleExceptionService.getSpecialistScheduleException(specialistId, start, end)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Choose period between 1 to 31 day", exception.getMessage());
    }

    // --- Тести для updateScheduleException ---

    @Test
    void updateScheduleException_Success() {
        UpdateScheduleExceptionRequest request = new UpdateScheduleExceptionRequest(
                LocalTime.of(10, 0),
                LocalTime.of(15, 0),
                null
        );

        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));
        when(scheduleExceptionRepository.findById(exceptionId)).thenReturn(Optional.of(scheduleException));
        when(scheduleExceptionRepository.findAllBySpecialistIdAndDateBetween(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(scheduleExceptionRepository.save(any(ScheduleException.class))).thenReturn(scheduleException);

        ScheduleExceptionResponse response = scheduleExceptionService.updateScheduleException(specialistId, exceptionId, request);

        assertNotNull(response);
        verify(scheduleExceptionRepository, times(1)).save(scheduleException);
    }

    @Test
    void updateScheduleException_ForbiddenOtherSpecialist() {
        UUID otherSpecialistId = UUID.randomUUID();
        Specialist otherSpecialist = new Specialist();
        otherSpecialist.setId(otherSpecialistId);
        scheduleException.setSpecialist(otherSpecialist); // Виключення належить іншому

        UpdateScheduleExceptionRequest request = new UpdateScheduleExceptionRequest(
                LocalTime.of(10, 0), LocalTime.of(15, 0), null
        );

        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));
        when(scheduleExceptionRepository.findById(exceptionId)).thenReturn(Optional.of(scheduleException));

        ApiException exception = assertThrows(ApiException.class, () ->
                scheduleExceptionService.updateScheduleException(specialistId, exceptionId, request)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Specialist can manage only its own schedule exceptions", exception.getMessage());
    }

    // --- Тести для deleteScheduleException ---

    @Test
    void deleteScheduleException_Success() {
        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));
        when(scheduleExceptionRepository.findById(exceptionId)).thenReturn(Optional.of(scheduleException));
        doNothing().when(scheduleExceptionRepository).delete(scheduleException);

        assertDoesNotThrow(() -> scheduleExceptionService.deleteScheduleException(specialistId, exceptionId));

        verify(scheduleExceptionRepository, times(1)).delete(scheduleException);
    }

    @Test
    void deleteScheduleException_NotFound() {
        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));
        when(scheduleExceptionRepository.findById(exceptionId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                scheduleExceptionService.deleteScheduleException(specialistId, exceptionId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Schedule exception not found", exception.getMessage());
        verify(scheduleExceptionRepository, never()).delete(any());
    }
}