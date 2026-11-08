package org.example.Appointment.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.Client.Entity.Client;
import org.example.Specialist.Entity.Specialist;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
    @Table(
            name = "specialist_clients",
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_specialist_clients_specialist_client",
                    columnNames = {"specialist_id", "client_id"}
            )
    )
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class SpecialistClient {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "specialist_id", nullable = false)
        private Specialist specialist;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "client_id", nullable = false)
        private Client client;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        private ClientStatus status;

        @Column(nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @Column(nullable = false)
        private LocalDateTime updatedAt;

        @PrePersist
        protected void onCreate() {
            LocalDateTime now = LocalDateTime.now();
            createdAt = now;
            updatedAt = now;
        }

        @PreUpdate
        protected void onUpdate() {
            updatedAt = LocalDateTime.now();
        }
    }
