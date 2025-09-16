package com.marchenko.clinicvisittracker.repository;

import com.marchenko.clinicvisittracker.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("""
            SELECT p FROM Patient p
            WHERE (:search IS NULL OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Patient> findAllWithSearch(@Param("search") String search, Pageable pageable);
}
