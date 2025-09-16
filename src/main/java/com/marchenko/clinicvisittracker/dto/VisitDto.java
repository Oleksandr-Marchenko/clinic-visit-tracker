package com.marchenko.clinicvisittracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class VisitDto {

    private LocalDateTime start;

    private LocalDateTime end;

    private DoctorDto doctor;
}
