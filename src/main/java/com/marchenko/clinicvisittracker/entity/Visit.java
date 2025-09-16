package com.marchenko.clinicvisittracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "visits")
public class Visit {

    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    private Doctor doctor;
}
