package org.example.WorkingHours.Service;

import org.example.Common.Exception.ApiException;
import org.example.Specialist.Entity.Specialist;
import org.example.Specialist.Repository.SpecialistRepository;
import org.example.WorkingHours.DTO.CreateWorkingHourRequest;
import org.example.WorkingHours.DTO.UpdateWorkingHoursRequest;
import org.example.WorkingHours.DTO.WorkingHoursResponse;
import org.example.WorkingHours.Entity.WorkingHours;
import org.example.WorkingHours.Repository.WorkingHoursRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkingHoursServiceTest {

    @Mock
    private WorkingHoursRepository workingHoursRepository;

    @Mock
    private SpecialistRepository specialistRepository;

    @InjectMocks
    private WorkingHoursService workingHoursService;

    private UUID specialistId;
    private UUID workingHoursId;
    private Specialist specialist;
    private WorkingHours workingHours;

    @BeforeEach
    void setUp() {
        specialistId = UUID.randomUUID();
        workingHoursId = UUID.randomUUID();

        specialist = new Specialist();
        specialist.setId(specialistId);

        workingHours = WorkingHours.builder()
                .id(workingHoursId)
                .specialist(specialist)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();
    }

    // --- Тести для createWorkingHours ---

    @Test
    void createWorkingHours_Success() {
        CreateWorkingHourRequest request = new CreateWorkingHourRequest(
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );

        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));
        when(workingHoursRepository.findAllWorkingHoursBySpecialist_Id(specialistId))
                .thenReturn(Collections.emptyList());
        when(workingHoursRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<WorkingHoursResponse> responses = workingHoursService.createWorkingHours(specialistId, List.of(request));

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(DayOfWeek.MONDAY, responses.get(0).day());
        verify(workingHoursRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createWorkingHours_SpecialistNotFound() {
        CreateWorkingHourRequest request = new CreateWorkingHourRequest(
                DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)
        );

        when(specialistRepository.findById(specialistId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                workingHoursService.createWorkingHours(specialistId, List.of(request))
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Specialist not found", exception.getMessage());
        verify(workingHoursRepository, never()).saveAll(any());
    }

    @Test
    void createWorkingHours_OverlapConflict() {
        CreateWorkingHourRequest request = new CreateWorkingHourRequest(
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(15, 0)
        );

        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));
        // Вже є робочі години на понеділок з 9 до 17, новий запит перетинається
        when(workingHoursRepository.findAllWorkingHoursBySpecialist_Id(specialistId))
                .thenReturn(List.of(workingHours));

        ApiException exception = assertThrows(ApiException.class, () ->
                workingHoursService.createWorkingHours(specialistId, List.of(request))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertTrue(exception.getMessage().contains("Working hours overlap with existing schedule"));
    }

    // --- Тести для getALLWorkingHoursBySpecialist ---

    @Test
    void getALLWorkingHoursBySpecialist_Success() {
        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));
        when(workingHoursRepository.findAllWorkingHoursBySpecialist_Id(specialistId))
                .thenReturn(List.of(workingHours));

        List<WorkingHoursResponse> responses = workingHoursService.getALLWorkingHoursBySpecialist(specialistId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(workingHoursId, responses.get(0).id());
    }

    @Test
    void getALLWorkingHoursBySpecialist_SpecialistNotFound() {
        when(specialistRepository.findById(specialistId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                workingHoursService.getALLWorkingHoursBySpecialist(specialistId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Specialist not found", exception.getMessage());
    }

    // --- Тести для updateWorkingHours ---

    @Test
    void updateWorkingHours_Success() {
        UpdateWorkingHoursRequest request = new UpdateWorkingHoursRequest(
                LocalTime.of(8, 0),
                LocalTime.of(16, 0)
        );

        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));
        when(workingHoursRepository.findById(workingHoursId)).thenReturn(Optional.of(workingHours));
        when(workingHoursRepository.findAllWorkingHoursBySpecialist_Id(specialistId))
                .thenReturn(List.of(workingHours));
        when(workingHoursRepository.save(any(WorkingHours.class))).thenReturn(workingHours);

        WorkingHoursResponse response = workingHoursService.updateWorkingHours(specialistId, workingHoursId, request);

        assertNotNull(response);
        assertEquals(LocalTime.of(8, 0), response.startTime());
        assertEquals(LocalTime.of(16, 0), response.endTime());
        verify(workingHoursRepository, times(1)).save(workingHours);
    }

    @Test
    void updateWorkingHours_ForbiddenOtherSpecialist() {
        UUID otherSpecialistId = UUID.randomUUID();
        Specialist otherSpecialist = new Specialist();
        otherSpecialist.setId(otherSpecialistId);
        workingHours.setSpecialist(otherSpecialist); // Належить іншому

        UpdateWorkingHoursRequest request = new UpdateWorkingHoursRequest(
                LocalTime.of(8, 0), LocalTime.of(16, 0)
        );

        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));
        when(workingHoursRepository.findById(workingHoursId)).thenReturn(Optional.of(workingHours));

        ApiException exception = assertThrows(ApiException.class, () ->
                workingHoursService.updateWorkingHours(specialistId, workingHoursId, request)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Specialist can manage only its own working hours", exception.getMessage());
    }

    // --- Тести для deleteWorkingHours ---

    @Test
    void deleteWorkingHours_Success() {
        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));
        when(workingHoursRepository.findById(workingHoursId)).thenReturn(Optional.of(workingHours));
        doNothing().when(workingHoursRepository).delete(workingHours);

        assertDoesNotThrow(() -> workingHoursService.deleteWorkingHours(specialistId, workingHoursId));

        verify(workingHoursRepository, times(1)).delete(workingHours);
    }

    @Test
    void deleteWorkingHours_NotFound() {
        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));
        when(workingHoursRepository.findById(workingHoursId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () ->
                workingHoursService.deleteWorkingHours(specialistId, workingHoursId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Working hours not found", exception.getMessage());
        verify(workingHoursRepository, never()).delete(any());
    }
}