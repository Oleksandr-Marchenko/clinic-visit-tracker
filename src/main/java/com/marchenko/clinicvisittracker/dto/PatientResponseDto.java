package com.marchenko.clinicvisittracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PatientResponseDto {

    private String firstName;

    private String lastName;

    private List<VisitDto> lastVisits;
}
