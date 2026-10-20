package org.example.WorkingHours.Repository;

import org.example.WorkingHours.Entity.ScheduleException;
import org.example.WorkingHours.Entity.WorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkingHoursRepository extends JpaRepository<WorkingHours, UUID> {
    List<WorkingHours> findAllWorkingHoursBySpecialist_Id(UUID specialistId);
    List<WorkingHours> findAllBySpecialistIdAndDayOfWeek(
            UUID specialistId,
            DayOfWeek day
    );
}