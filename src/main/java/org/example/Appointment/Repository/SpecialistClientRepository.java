package org.example.Appointment.Repository;

import org.example.Appointment.Entity.SpecialistClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpecialistClientRepository
        extends JpaRepository<SpecialistClient, UUID> {

    Optional<SpecialistClient> findBySpecialist_IdAndClient_Id(
            UUID specialistId,
            UUID clientId
    );
    List<SpecialistClient> findAllBySpecialist_Id(UUID specialistId);
}
