package com.sgl.backend.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder
public class AttendanceSummaryDTO {
    private Integer totalUsers;
    private Integer totalAttendances;
    private LocalDate firstAttendance;
    private LocalDate lastAttendance;
}
