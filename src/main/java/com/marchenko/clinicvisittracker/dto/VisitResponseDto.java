package com.marchenko.clinicvisittracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitResponseDto {
    private Long id;
    private String start;
    private String end;
    private Long patientId;
    private Long doctorId;
}
