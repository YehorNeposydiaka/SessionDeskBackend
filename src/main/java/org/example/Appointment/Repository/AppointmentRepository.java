package org.example.Appointment.Repository;

import org.example.Appointment.Entity.Appointment;
import org.example.Appointment.Entity.ClientActivityProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findAllBySpecialistIdAndDateBetween(
            UUID specialistId,
            LocalDate startDate,
            LocalDate endDate
    );
    Optional<Appointment> findByIdAndSpecialistId(UUID id, UUID specialistId);
    Optional<Appointment> findByIdAndClientId(UUID id, UUID clientId);

    @Query(value = """
        SELECT 
            id, 
            'APPOINTMENT' AS item_type, 
            created_at AS event_time, 
            CAST(status AS VARCHAR) AS title, 
            note AS details 
        FROM appointments 
        WHERE specialist_id = :specialistId AND client_id = :clientId
        
        UNION ALL
        
        SELECT 
            id, 
            'NOTE' AS item_type, 
            created_at AS event_time, 
            note_name AS title, 
            attachment_url AS details 
        FROM notes 
        WHERE specialist_id = :specialistId AND client_id = :clientId
        
        ORDER BY event_time DESC
        """,
            countQuery = """
        SELECT count(*) FROM (
            SELECT id FROM appointments WHERE specialist_id = :specialistId AND client_id = :clientId
            UNION ALL
            SELECT id FROM notes WHERE specialist_id = :specialistId AND client_id = :clientId
        ) as combined_activities
        """,
            nativeQuery = true)
    Page<ClientActivityProjection> findClientActivities(
            @Param("specialistId") UUID specialistId,
            @Param("clientId") UUID clientId,
            Pageable pageable
    );
}