package com.sgl.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.sgl.backend.entity.PracticeType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PracticeResponse {
    private Long id;
    private String subject;
    private PracticeType practiceType;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;
    private Integer studentCount;
    private String laboratoryName;
}
