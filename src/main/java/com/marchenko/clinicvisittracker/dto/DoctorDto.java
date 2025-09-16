package com.marchenko.clinicvisittracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoctorDto {

    private String firstName;

    private String lastName;

    private int totalPatients;
}
