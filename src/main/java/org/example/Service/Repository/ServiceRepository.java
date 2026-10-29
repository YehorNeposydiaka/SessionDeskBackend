package org.example.Service.Repository;

import org.example.Service.Entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<Service, UUID> {
    List<Service> findAllServiceBySpecialist_Id(UUID specialistId);

    Optional<Service> findByIdAndSpecialist_Id(UUID id, UUID specialistId);
}