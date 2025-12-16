package com.sgl.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceResponse {
    private Long id;
    private String monitorName;
    private String monitorCode;
    private LocalDate date;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Double hoursWorked;
    private String status;
}
