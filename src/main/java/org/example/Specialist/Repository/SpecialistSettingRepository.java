package org.example.Specialist.Repository;

import org.example.Specialist.Entity.SpecialistSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpecialistSettingRepository extends JpaRepository<SpecialistSettings, UUID> {
}
