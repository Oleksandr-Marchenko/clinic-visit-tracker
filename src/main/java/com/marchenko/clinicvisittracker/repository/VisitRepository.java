package com.marchenko.clinicvisittracker.repository;

import com.marchenko.clinicvisittracker.entity.Patient;
import com.marchenko.clinicvisittracker.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    @Query("""
            SELECT COUNT(v) > 0
            FROM Visit v
            WHERE v.doctor.id = :doctorId
            AND (
                  (:start BETWEEN v.startDateTime AND v.endDateTime) OR
                  (:end BETWEEN v.startDateTime AND v.endDateTime) OR
                  (v.startDateTime BETWEEN :start AND :end)
                )
            """)
    boolean existsByDoctorAndTimeOverlap(@Param("doctorId") Long doctorId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query("""
            SELECT v FROM Visit v
            WHERE v.patient IN :patients
            AND (:doctorIds IS NULL OR v.doctor.id IN :doctorIds)
            AND v.startDateTime = (
                SELECT MAX(v2.startDateTime)
                FROM Visit v2
                WHERE v2.patient = v.patient AND v2.doctor = v.doctor
            )
            """)
    List<Visit> findLastVisitsForPatients(@Param("patients") List<Patient> patients,
                                          @Param("doctorIds") List<Long> doctorIds);

    @Query("""
            SELECT v.doctor.id, COUNT(DISTINCT v.patient)
            FROM Visit v
            GROUP BY v.doctor.id
            """)
    Map<Long, Long> countPatientsPerDoctor();
}
