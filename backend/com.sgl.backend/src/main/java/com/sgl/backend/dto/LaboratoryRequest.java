package com.sgl.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LaboratoryRequest {
    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Capacity is mandatory")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
}
