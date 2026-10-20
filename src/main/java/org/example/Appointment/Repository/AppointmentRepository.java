package org.example.Appointment.Repository;

import org.example.Appointment.Entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findAllBySpecialistIdAndDateBetween(
            UUID specialistId,
            LocalDate startDate,
            LocalDate endDate
    );
}