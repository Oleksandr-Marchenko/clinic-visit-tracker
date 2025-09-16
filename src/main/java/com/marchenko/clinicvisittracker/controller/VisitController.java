package com.marchenko.clinicvisittracker.controller;

import com.marchenko.clinicvisittracker.dto.VisitRequestDto;
import com.marchenko.clinicvisittracker.entity.Visit;
import com.marchenko.clinicvisittracker.service.VisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class VisitController {

    private final VisitService visitService;

    @PostMapping("/visits")
    public Visit createVisit(@Valid @RequestBody VisitRequestDto visitRequest) {
        return visitService.createVisit(visitRequest);
    }
}