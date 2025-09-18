package com.marchenko.clinicvisittracker.service;

import com.marchenko.clinicvisittracker.dto.VisitRequestDto;
import com.marchenko.clinicvisittracker.dto.VisitResponseDto;
import com.marchenko.clinicvisittracker.entity.Doctor;
import com.marchenko.clinicvisittracker.entity.Patient;
import com.marchenko.clinicvisittracker.entity.Visit;
import com.marchenko.clinicvisittracker.repository.DoctorRepository;
import com.marchenko.clinicvisittracker.repository.PatientRepository;
import com.marchenko.clinicvisittracker.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;

    private final PatientRepository patientRepository;

    private final DoctorRepository doctorRepository;

    @Transactional
    public VisitResponseDto createVisit(VisitRequestDto visitRequest) {

        Doctor doctor = doctorRepository.findById(visitRequest.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        if (!patientRepository.existsById(visitRequest.getPatientId())) {
            throw new IllegalArgumentException("Patient not found");
        }
        Patient patient = patientRepository.getReferenceById(visitRequest.getPatientId());

        ZoneId doctorZone = resolveZoneId(doctor.getTimezone());

        ZonedDateTime parsedStart;
        ZonedDateTime parsedEnd;
        try {
            parsedStart = ZonedDateTime.parse(visitRequest.getStart());
        } catch (Exception ex) {
            LocalDateTime localStart = LocalDateTime.parse(visitRequest.getStart());
            parsedStart = localStart.atZone(doctorZone);
        }
        try {
            parsedEnd = ZonedDateTime.parse(visitRequest.getEnd());
        } catch (Exception ex) {
            LocalDateTime localEnd = LocalDateTime.parse(visitRequest.getEnd());
            parsedEnd = localEnd.atZone(doctorZone);
        }

        ZonedDateTime start = parsedStart.withZoneSameInstant(doctorZone);
        ZonedDateTime end = parsedEnd.withZoneSameInstant(doctorZone);

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start must be before end");
        }

        if (visitRepository.existsByDoctorAndTimeOverlap(doctor.getId(),
                start.toLocalDateTime(), end.toLocalDateTime())) {
            throw new IllegalArgumentException("Doctor is already booked for this time");
        }

        Visit visit = new Visit();
        visit.setDoctor(doctor);
        visit.setPatient(patient);
        visit.setStartDateTime(start.toLocalDateTime());
        visit.setEndDateTime(end.toLocalDateTime());

        Visit saved = visitRepository.save(visit);
        return new VisitResponseDto(
                saved.getId(),
                start.toLocalDateTime().toString(),
                end.toLocalDateTime().toString(),
                patient.getId(),
                doctor.getId()
        );
    }

    private ZoneId resolveZoneId(String timezoneId) {
        try {
            return ZoneId.of(timezoneId);
        } catch (Exception ex) {
            if ("Europe/Kyiv".equalsIgnoreCase(timezoneId)) {
                return ZoneId.of("Europe/Kiev");
            }
            throw ex;
        }
    }
}
