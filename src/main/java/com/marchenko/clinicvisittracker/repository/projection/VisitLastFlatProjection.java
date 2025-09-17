package com.marchenko.clinicvisittracker.repository.projection;

import java.time.LocalDateTime;

public interface VisitLastFlatProjection {
    Long getPatientId();
    Long getDoctorId();
    LocalDateTime getStartDateTime();
    LocalDateTime getEndDateTime();
    String getDoctorFirstName();
    String getDoctorLastName();
}


