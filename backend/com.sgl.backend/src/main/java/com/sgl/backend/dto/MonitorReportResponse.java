package com.sgl.backend.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonitorReportResponse {
    private String monitorCode;
    private String monitorName;
    private Integer totalDaysWorked;
    private Double totalHoursWorked;
    private List<AttendanceResponse> attendances;
}
