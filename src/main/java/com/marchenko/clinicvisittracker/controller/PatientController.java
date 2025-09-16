package com.marchenko.clinicvisittracker.controller;

import com.marchenko.clinicvisittracker.dto.PagedResponseDto;
import com.marchenko.clinicvisittracker.dto.PatientResponseDto;
import com.marchenko.clinicvisittracker.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/patients")
    public PagedResponseDto<PatientResponseDto> getPatients(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Long> doctorIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return patientService.getPatients(search, doctorIds, page, size);
    }
}
