package com.marchenko.clinicvisittracker.service;

import com.marchenko.clinicvisittracker.dto.VisitRequestDto;
import com.marchenko.clinicvisittracker.entity.Doctor;
import com.marchenko.clinicvisittracker.entity.Patient;
import com.marchenko.clinicvisittracker.dto.VisitResponseDto;
import com.marchenko.clinicvisittracker.repository.DoctorRepository;
import com.marchenko.clinicvisittracker.repository.PatientRepository;
import com.marchenko.clinicvisittracker.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

    @Mock
    private VisitRepository visitRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private VisitService visitService;

    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        doctor = new Doctor(1L, "Ihor", "Karpenko", "Europe/Kyiv");
        patient = new Patient(2L, "Ivan", "Petrenko");
    }

    @Test
    void throws_if_doctor_not_found() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());
        VisitRequestDto dto = new VisitRequestDto("2025-01-01T09:00:00", "2025-01-01T10:00:00", 2L, 1L);
        assertThrows(IllegalArgumentException.class, () -> visitService.createVisit(dto));
    }

    @Test
    void throws_if_patient_not_found() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(patientRepository.existsById(2L)).thenReturn(false);
        VisitRequestDto dto = new VisitRequestDto("2025-01-01T09:00:00", "2025-01-01T10:00:00", 2L, 1L);
        assertThrows(IllegalArgumentException.class, () -> visitService.createVisit(dto));
    }

    @Test
    void throws_if_start_not_before_end() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(patientRepository.existsById(2L)).thenReturn(true);
        VisitRequestDto dto = new VisitRequestDto("2025-01-01T10:00:00", "2025-01-01T09:00:00", 2L, 1L);
        assertThrows(IllegalArgumentException.class, () -> visitService.createVisit(dto));
    }

    @Test
    void throws_if_overlap_exists() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(patientRepository.existsById(2L)).thenReturn(true);
        when(visitRepository.existsByDoctorAndTimeOverlap(eq(1L), any(), any())).thenReturn(true);

        VisitRequestDto dto = new VisitRequestDto("2025-01-01T09:00:00", "2025-01-01T10:00:00", 2L, 1L);
        assertThrows(IllegalArgumentException.class, () -> visitService.createVisit(dto));
    }

    @Test
    void saves_visit_when_valid_no_overlap() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(patientRepository.existsById(2L)).thenReturn(true);
        when(patientRepository.getReferenceById(2L)).thenReturn(patient);
        when(visitRepository.existsByDoctorAndTimeOverlap(eq(1L), any(), any())).thenReturn(false);
        when(visitRepository.save(any())).thenAnswer(inv -> {
            com.marchenko.clinicvisittracker.entity.Visit v = inv.getArgument(0);
            v.setId(100L);
            return v;
        });

        VisitRequestDto dto = new VisitRequestDto("2025-01-01T09:00:00", "2025-01-01T10:00:00", 2L, 1L);
        VisitResponseDto saved = visitService.createVisit(dto);
        assertThat(saved.getId()).isEqualTo(100L);
        verify(visitRepository).save(any());
    }
}


