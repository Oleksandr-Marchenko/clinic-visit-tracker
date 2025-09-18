package com.marchenko.clinicvisittracker.repository;

import com.marchenko.clinicvisittracker.entity.Doctor;
import com.marchenko.clinicvisittracker.entity.Patient;
import com.marchenko.clinicvisittracker.entity.Visit;
import com.marchenko.clinicvisittracker.repository.projection.VisitLastFlatProjection;
import com.marchenko.clinicvisittracker.testsupport.MySQLTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VisitRepositoryIT extends MySQLTestContainer {

    @Autowired
    private VisitRepository visitRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private PatientRepository patientRepository;

    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void init() {
        visitRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        doctor = doctorRepository.save(new Doctor(null, "Ihor", "Karpenko", "Europe/Kyiv"));
        patient = patientRepository.save(new Patient(null, "Ivan", "Petrenko"));
    }

    @Test
    void overlap_exists_when_times_intersect() {
        visitRepository.save(new Visit(null,
                LocalDateTime.of(2025, 1, 1, 10, 0),
                LocalDateTime.of(2025, 1, 1, 11, 0),
                patient, doctor));

        boolean overlaps = visitRepository.existsByDoctorAndTimeOverlap(
                doctor.getId(),
                LocalDateTime.of(2025, 1, 1, 10, 30),
                LocalDateTime.of(2025, 1, 1, 11, 30)
        );
        assertThat(overlaps).isTrue();

        boolean noOverlap = visitRepository.existsByDoctorAndTimeOverlap(
                doctor.getId(),
                LocalDateTime.of(2025, 1, 1, 11, 0),
                LocalDateTime.of(2025, 1, 1, 12, 0)
        );
        assertThat(noOverlap).isFalse();
    }

    @Test
    void last_visit_via_window_function() {
        visitRepository.save(new Visit(null,
                LocalDateTime.of(2025, 1, 1, 9, 0),
                LocalDateTime.of(2025, 1, 1, 10, 0),
                patient, doctor));
        Visit latest = visitRepository.save(new Visit(null,
                LocalDateTime.of(2025, 1, 2, 9, 0),
                LocalDateTime.of(2025, 1, 2, 10, 0),
                patient, doctor));

        List<VisitLastFlatProjection> result = visitRepository.findLastVisitsForPatientsFlat(
                List.of(patient.getId()), List.of(doctor.getId()), true);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartDateTime()).isEqualTo(latest.getStartDateTime());
        assertThat(result.get(0).getDoctorFirstName()).isEqualTo(doctor.getFirstName());
    }
}


