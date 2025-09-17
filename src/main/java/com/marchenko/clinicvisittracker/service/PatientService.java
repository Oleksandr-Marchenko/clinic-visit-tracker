package com.marchenko.clinicvisittracker.service;

import com.marchenko.clinicvisittracker.dto.DoctorDto;
import com.marchenko.clinicvisittracker.dto.PagedResponseDto;
import com.marchenko.clinicvisittracker.dto.PatientResponseDto;
import com.marchenko.clinicvisittracker.dto.VisitDto;
import com.marchenko.clinicvisittracker.entity.Patient;
import com.marchenko.clinicvisittracker.repository.PatientRepository;
import com.marchenko.clinicvisittracker.repository.VisitRepository;
import com.marchenko.clinicvisittracker.repository.projection.VisitLastFlatProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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

        List<Long> patientIds = patientsPage.stream().map(Patient::getId).toList();

        boolean applyDoctorFilter = doctorIds != null && !doctorIds.isEmpty();
        List<Long> safeDoctorIds = applyDoctorFilter ? doctorIds : null;

        List<VisitLastFlatProjection> visits = patientIds.isEmpty()
                ? List.of()
                : visitRepository.findLastVisitsForPatientsFlat(patientIds, safeDoctorIds, applyDoctorFilter);

        List<Long> doctorIdsUsed = visits.stream().map(VisitLastFlatProjection::getDoctorId).distinct().toList();
        Map<Long, Integer> totalPatientsMap = new HashMap<>();
        if (!doctorIdsUsed.isEmpty()) {
            List<Object[]> rows = visitRepository.countPatientsPerDoctorFor(doctorIdsUsed);
            for (Object[] row : rows) {
                Long dId = ((Number) row[0]).longValue();
                Integer cnt = ((Number) row[1]).intValue();
                totalPatientsMap.put(dId, cnt);
            }
        }

        List<PatientResponseDto> patientDtos = patientsPage.stream().map(patient -> {
            List<VisitDto> lastVisits = visits.stream()
                    .filter(v -> v.getPatientId().equals(patient.getId()))
                    .map(v -> new VisitDto(
                            v.getStartDateTime(),
                            v.getEndDateTime(),
                            new DoctorDto(
                                    v.getDoctorFirstName(),
                                    v.getDoctorLastName(),
                                    totalPatientsMap.getOrDefault(v.getDoctorId(), 0)
                            )
                    ))
                    .collect(Collectors.toList());
            return new PatientResponseDto(patient.getFirstName(), patient.getLastName(), lastVisits);
        }).toList();

        return new PagedResponseDto<>(patientDtos, patientsPage.getTotalElements());
    }
}
