package org.example.WorkingHours.Repository;

import org.example.WorkingHours.Entity.ScheduleException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ScheduleExceptionRepository extends JpaRepository<ScheduleException, UUID> {
    List<ScheduleException> findAllBySpecialistIdAndDate(UUID specialistId, LocalDate date);
    List<ScheduleException> findAllBySpecialistIdAndDateBetween(
            UUID specialistId,
            LocalDate startDate,
            LocalDate endDate
    );
}