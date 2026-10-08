package org.example.WorkingHours.Repository;

import org.example.WorkingHours.Entity.ScheduleException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScheduleExceptionRepository extends JpaRepository<ScheduleException, UUID> {
}