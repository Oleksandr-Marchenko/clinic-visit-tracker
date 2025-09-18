package com.marchenko.clinicvisittracker.service;

import com.marchenko.clinicvisittracker.dto.PagedResponseDto;
import com.marchenko.clinicvisittracker.dto.PatientResponseDto;
import com.marchenko.clinicvisittracker.entity.Doctor;
import com.marchenko.clinicvisittracker.entity.Patient;
import com.marchenko.clinicvisittracker.entity.Visit;
import com.marchenko.clinicvisittracker.repository.DoctorRepository;
import com.marchenko.clinicvisittracker.repository.PatientRepository;
import com.marchenko.clinicvisittracker.repository.VisitRepository;
import com.marchenko.clinicvisittracker.testsupport.MySQLTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PatientServiceIT extends MySQLTestContainer {

    @Autowired
    private PatientService patientService;
    @Autowired
    private VisitRepository visitRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private PatientRepository patientRepository;

    private Doctor doctor1;
    private Doctor doctor2;
    private Patient patient1;
    private Patient patient2;

    @BeforeEach
    void setup() {
        visitRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        doctor1 = doctorRepository.save(new Doctor(null, "D1", "A", "Europe/Kyiv"));
        doctor2 = doctorRepository.save(new Doctor(null, "D2", "B", "Europe/Kyiv"));
        patient1 = patientRepository.save(new Patient(null, "Ivan", "Petrenko"));
        patient2 = patientRepository.save(new Patient(null, "Olga", "Shevchenko"));

        visitRepository.save(new Visit(null,
                LocalDateTime.of(2025, 1, 1, 9, 0),
                LocalDateTime.of(2025, 1, 1, 10, 0), patient1, doctor1));
        visitRepository.save(new Visit(null,
                LocalDateTime.of(2025, 1, 2, 9, 0),
                LocalDateTime.of(2025, 1, 2, 10, 0), patient1, doctor1));
        visitRepository.save(new Visit(null,
                LocalDateTime.of(2025, 1, 3, 9, 0),
                LocalDateTime.of(2025, 1, 3, 10, 0), patient1, doctor2));
        visitRepository.save(new Visit(null,
                LocalDateTime.of(2025, 1, 4, 9, 0),
                LocalDateTime.of(2025, 1, 4, 10, 0), patient2, doctor1));
    }

    @Test
    void getPatients_filters_by_doctorIds_and_builds_lastVisits_and_counts() {
        PagedResponseDto<PatientResponseDto> page = patientService.getPatients(null, List.of(doctor1.getId()), 0, 10);
        assertThat(page.getData()).hasSize(2);
        PatientResponseDto first = page.getData().stream()
                .filter(p -> p.getFirstName().equals("Ivan")).findFirst().orElseThrow();
        assertThat(first.getLastVisits()).hasSize(1);
        assertThat(first.getLastVisits().get(0).getDoctor().getTotalPatients()).isEqualTo(2);
    }

    @Test
    void getPatients_search_works_case_insensitive() {
        PagedResponseDto<PatientResponseDto> page = patientService.getPatients("iv", null, 0, 10);
        assertThat(page.getData()).extracting(PatientResponseDto::getFirstName).contains("Ivan");
    }
}


