package org.example.WorkingHours.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.Client.Entity.Client;
import org.example.Specialist.Entity.Specialist;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "working_hours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkingHours {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialist_id", nullable = false)
    private Specialist specialist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;
}
