package com.sgl.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceRequest {
    @NotBlank
    private String monitorCode;

    @NotNull
    private AttendanceType type;

    public enum AttendanceType {
        CHECK_IN, CHECK_OUT
    }
}
