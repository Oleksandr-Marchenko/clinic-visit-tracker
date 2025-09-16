package com.marchenko.clinicvisittracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PagedResponseDto<T> {

    private List<T> data;

    private long count;
}
