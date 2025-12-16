package com.sgl.backend.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder
public class AttendanceReportDTO {
    private String userCode;
    private String userName;
    private String userRole;
    private Integer totalAttendances;
    private List<LocalDate> attendanceDates;
}
