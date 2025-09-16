package com.marchenko.clinicvisittracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitRequestDto {

    @NotNull
    private String start;

    @NotNull
    private String end;

    @NotNull
    private Long patientId;

    @NotNull
    private Long doctorId;
}
