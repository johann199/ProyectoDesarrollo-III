package com.sgl.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanRequest {

    @NotBlank(message = "Barcode is mandatory")
    private String barcode;

    @NotBlank(message = "Student code is mandatory")
    private String studentCode;

    @NotBlank(message = "Monitor code is mandatory")
    private String monitorCode;
}
