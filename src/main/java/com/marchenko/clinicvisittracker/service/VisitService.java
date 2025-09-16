package com.marchenko.clinicvisittracker.service;

import com.marchenko.clinicvisittracker.dto.VisitRequestDto;
import com.marchenko.clinicvisittracker.entity.Doctor;
import com.marchenko.clinicvisittracker.entity.Patient;
import com.marchenko.clinicvisittracker.entity.Visit;
import com.marchenko.clinicvisittracker.repository.DoctorRepository;
import com.marchenko.clinicvisittracker.repository.PatientRepository;
import com.marchenko.clinicvisittracker.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;

    private final PatientRepository patientRepository;

    private final DoctorRepository doctorRepository;

    @Transactional
    public Visit createVisit(VisitRequestDto visitRequest) {

        Doctor doctor = doctorRepository.findById(visitRequest.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        Patient patient = patientRepository.findById(visitRequest.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        ZoneId doctorZone = ZoneId.of(doctor.getTimezone());
        ZonedDateTime start = ZonedDateTime.parse(visitRequest.getStart()).withZoneSameInstant(doctorZone);
        ZonedDateTime end = ZonedDateTime.parse(visitRequest.getEnd()).withZoneSameInstant(doctorZone);

        if (visitRepository.existsByDoctorAndTimeOverlap(doctor.getId(),
                start.toLocalDateTime(), end.toLocalDateTime())) {
            throw new IllegalArgumentException("Doctor is already booked for this time");
        }

        Visit visit = new Visit();
        visit.setDoctor(doctor);
        visit.setPatient(patient);
        visit.setStartDateTime(start.toLocalDateTime());
        visit.setEndDateTime(end.toLocalDateTime());

        return visitRepository.save(visit);
    }
}
