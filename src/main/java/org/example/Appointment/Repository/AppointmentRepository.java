package org.example.Appointment.Repository;

import org.example.Appointment.Entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
}