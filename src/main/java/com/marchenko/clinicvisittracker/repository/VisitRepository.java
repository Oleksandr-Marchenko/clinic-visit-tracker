package com.marchenko.clinicvisittracker.repository;

import com.marchenko.clinicvisittracker.entity.Visit;
import com.marchenko.clinicvisittracker.repository.projection.VisitLastFlatProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(v) > 0 THEN TRUE ELSE FALSE END
            FROM Visit v
            WHERE v.doctor.id = :doctorId
            AND NOT (v.endDateTime <= :start OR v.startDateTime >= :end)
            """)
    boolean existsByDoctorAndTimeOverlap(@Param("doctorId") Long doctorId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query(value = """
            SELECT t.patient_id       AS patientId,
                   t.doctor_id        AS doctorId,
                   t.start_date_time  AS startDateTime,
                   t.end_date_time    AS endDateTime,
                   d.first_name       AS doctorFirstName,
                   d.last_name        AS doctorLastName
            FROM (
                 SELECT v.patient_id,
                        v.doctor_id,
                        v.start_date_time,
                        v.end_date_time,
                        ROW_NUMBER() OVER (PARTITION BY v.patient_id, v.doctor_id ORDER BY v.start_date_time DESC) AS rn
                 FROM visits v
                 WHERE v.patient_id IN (:patientIds)
                   AND (:applyDoctorFilter = FALSE OR v.doctor_id IN (:doctorIds))
            ) t
            JOIN doctors d ON d.id = t.doctor_id
            WHERE t.rn = 1
            """, nativeQuery = true)
    List<VisitLastFlatProjection> findLastVisitsForPatientsFlat(@Param("patientIds") List<Long> patientIds,
                                                                @Param("doctorIds") List<Long> doctorIds,
                                                                @Param("applyDoctorFilter") boolean applyDoctorFilter);

    @Query(value = """
            SELECT v.doctor_id AS doctorId, COUNT(DISTINCT v.patient_id) AS totalPatients
            FROM visits v
            WHERE v.doctor_id IN (:doctorIds)
            GROUP BY v.doctor_id
            """,
            nativeQuery = true)
    List<Object[]> countPatientsPerDoctorFor(@Param("doctorIds") List<Long> doctorIds);
}
