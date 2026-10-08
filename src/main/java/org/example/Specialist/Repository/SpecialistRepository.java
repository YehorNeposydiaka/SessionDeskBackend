package org.example.Specialist.Repository;

import org.example.Specialist.Entity.Specialist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpecialistRepository extends JpaRepository<Specialist, UUID> {
}