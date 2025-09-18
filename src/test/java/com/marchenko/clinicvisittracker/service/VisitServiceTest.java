package com.marchenko.clinicvisittracker.service;

import com.marchenko.clinicvisittracker.dto.VisitRequestDto;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class VisitServiceTest extends MySQLTestContainer {

    @Autowired
    private VisitService visitService;
    @Autowired
    private VisitRepository visitRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private PatientRepository patientRepository;

    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setup() {
        visitRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        doctor = doctorRepository.save(new Doctor(null, "Ihor", "Karpenko", "Europe/Kyiv"));
        patient = patientRepository.save(new Patient(null, "Ivan", "Petrenko"));
    }

    @Test
    @Transactional
    void createVisit_accepts_local_time_and_validates_order() {
        String start = LocalDateTime.of(2025, 1, 1, 10, 0).toString();
        String end = LocalDateTime.of(2025, 1, 1, 11, 0).toString();
        VisitRequestDto dto = new VisitRequestDto(start, end, patient.getId(), doctor.getId());
        Visit visit = visitService.createVisit(dto);
        assertThat(visit.getId()).isNotNull();

        VisitRequestDto invalid = new VisitRequestDto(end, start, patient.getId(), doctor.getId());
        assertThrows(IllegalArgumentException.class, () -> visitService.createVisit(invalid));
    }

    @Test
    void createVisit_prevents_overlap() {
        VisitRequestDto dto1 = new VisitRequestDto(
                LocalDateTime.of(2025, 1, 1, 10, 0).toString(),
                LocalDateTime.of(2025, 1, 1, 11, 0).toString(),
                patient.getId(), doctor.getId());
        visitService.createVisit(dto1);

        VisitRequestDto dto2 = new VisitRequestDto(
                LocalDateTime.of(2025, 1, 1, 10, 30).toString(),
                LocalDateTime.of(2025, 1, 1, 11, 30).toString(),
                patient.getId(), doctor.getId());
        assertThrows(IllegalArgumentException.class, () -> visitService.createVisit(dto2));
    }
}


