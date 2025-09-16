package com.marchenko.clinicvisittracker.service;

import com.marchenko.clinicvisittracker.dto.DoctorDto;
import com.marchenko.clinicvisittracker.dto.PagedResponseDto;
import com.marchenko.clinicvisittracker.dto.PatientResponseDto;
import com.marchenko.clinicvisittracker.dto.VisitDto;
import com.marchenko.clinicvisittracker.entity.Patient;
import com.marchenko.clinicvisittracker.entity.Visit;
import com.marchenko.clinicvisittracker.repository.PatientRepository;
import com.marchenko.clinicvisittracker.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;

    @Transactional(readOnly = true)
    public PagedResponseDto<PatientResponseDto> getPatients(String search, List<Long> doctorIds, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Patient> patientsPage = patientRepository.findAllWithSearch(search, pageable);

        List<Visit> visits = visitRepository.findLastVisitsForPatients(patientsPage.getContent(), doctorIds);

        Map<Long, Long> totalPatientsMap = visitRepository.countPatientsPerDoctor();

        List<PatientResponseDto> patientDtos = patientsPage.stream().map(p -> {
            List<VisitDto> lastVisits = visits.stream()
                    .filter(v -> v.getPatient().getId().equals(p.getId()))
                    .map(v -> new VisitDto(
                            v.getStartDateTime(),
                            v.getEndDateTime(),
                            new DoctorDto(
                                    v.getDoctor().getFirstName(),
                                    v.getDoctor().getLastName(),
                                    totalPatientsMap.getOrDefault(v.getDoctor().getId(), 0L).intValue()
                            )
                    ))
                    .collect(Collectors.toList());
            return new PatientResponseDto(p.getFirstName(), p.getLastName(), lastVisits);
        }).toList();

        return new PagedResponseDto<>(patientDtos, patientsPage.getTotalElements());
    }
}
