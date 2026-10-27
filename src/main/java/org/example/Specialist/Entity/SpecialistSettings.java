package org.example.Specialist.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.Service.Entity.Service;

import java.util.UUID;

@Entity
@Table(name = "specialist_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecialistSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialist_id", nullable = false, unique = true)
    private Specialist specialist;

    @Column(nullable = false)
    @Builder.Default
    private int maxBookingDaysAhead = 30;

    @Column(nullable = false)
    @Builder.Default
    private Integer minBookingNoticeHours = 8;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service defaultService;

    @Column(nullable = false)
    @Builder.Default
    private boolean isServiceSelectable = true;
}