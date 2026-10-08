package org.example.WorkingHours.Repository;

import org.example.WorkingHours.Entity.WorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkingHoursRepository extends JpaRepository<WorkingHours, UUID> {
}