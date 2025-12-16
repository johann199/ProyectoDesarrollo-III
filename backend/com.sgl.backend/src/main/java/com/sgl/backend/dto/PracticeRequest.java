package com.sgl.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.sgl.backend.entity.PracticeType;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PracticeRequest {
    @NotBlank(message = "Subject is mandatory")
    private String subject;

    private String laboratoryName;

    @NotNull(message = "Practice type is mandatory")
    private PracticeType practiceType;

    @NotNull(message = "Date is mandatory")
    @FutureOrPresent(message = "Date must be in the present or future")
    private LocalDate date;

    @NotNull(message = "Start time is mandatory")
    private LocalTime startTime;

    @NotNull(message = "Duration is mandatory")
    @Min(value = 30, message = "Duration must be at least 30 minutes")
    @Max(value = 240, message = "Duration must not exceed 4 hours (240 minutes)")
    private Integer durationMinutes;

    @NotNull(message = "Student count is mandatory")
    @Min(value = 1, message = "There must be at least 1 student")
    private Integer studentCount;
}
