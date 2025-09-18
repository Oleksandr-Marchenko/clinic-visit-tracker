package com.marchenko.clinicvisittracker.service;

import com.marchenko.clinicvisittracker.dto.PagedResponseDto;
import com.marchenko.clinicvisittracker.dto.PatientResponseDto;
import com.marchenko.clinicvisittracker.entity.Patient;
import com.marchenko.clinicvisittracker.repository.PatientRepository;
import com.marchenko.clinicvisittracker.repository.VisitRepository;
import com.marchenko.clinicvisittracker.repository.projection.VisitLastFlatProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    interface V extends VisitLastFlatProjection {}

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private VisitRepository visitRepository;

    @InjectMocks
    private PatientService patientService;

    @Test
    void returns_empty_when_no_patients() {
        when(patientRepository.findAllWithSearch(null, PageRequest.of(0, 20)))
                .thenReturn(Page.empty());
        PagedResponseDto<PatientResponseDto> res = patientService.getPatients(null, null, 0, 20);
        assertThat(res.getData()).isEmpty();
        assertThat(res.getCount()).isEqualTo(0);
    }

    @Test
    void builds_dto_from_flat_projection() {
        var p = new Patient(1L, "Ivan", "Petrenko");
        when(patientRepository.findAllWithSearch(null, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(p)));

        VisitLastFlatProjection proj = new VisitLastFlatProjection() {
            @Override
            public Long getPatientId() { return 1L; }
            @Override
            public Long getDoctorId() { return 5L; }
            @Override
            public LocalDateTime getStartDateTime() { return LocalDateTime.of(2025, 1, 1, 9, 0); }
            @Override
            public LocalDateTime getEndDateTime() { return LocalDateTime.of(2025, 1, 1, 10, 0); }
            @Override
            public String getDoctorFirstName() { return "Ihor"; }
            @Override
            public String getDoctorLastName() { return "Karpenko"; }
        };

        when(visitRepository.findLastVisitsForPatientsFlat(
                eq(List.of(1L)),
                Mockito.nullable(List.class),
                eq(false)
        )).thenReturn(List.of(proj));

        when(visitRepository.countPatientsPerDoctorFor(eq(List.of(5L))))
                .thenReturn(List.<Object[]>of(new Object[]{5L, 2L}));

        PagedResponseDto<PatientResponseDto> res = patientService.getPatients(null, null, 0, 10);

        assertThat(res.getData()).hasSize(1);
        var dto = res.getData().get(0);
        assertThat(dto.getFirstName()).isEqualTo("Ivan");
        assertThat(dto.getLastVisits()).hasSize(1);

        var lastVisit = dto.getLastVisits().get(0);
        assertThat(lastVisit.getStart()).isEqualTo(proj.getStartDateTime());
        assertThat(lastVisit.getEnd()).isEqualTo(proj.getEndDateTime());
        assertThat(lastVisit.getDoctor().getFirstName()).isEqualTo("Ihor");
        assertThat(lastVisit.getDoctor().getLastName()).isEqualTo("Karpenko");
        assertThat(lastVisit.getDoctor().getTotalPatients()).isEqualTo(2);
    }
}


