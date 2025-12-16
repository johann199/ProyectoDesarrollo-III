package com.sgl.backend.dto;

import java.time.LocalDateTime;

import com.sgl.backend.entity.Loan.LoanStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanResponse {
    private Long id;
    private String equipmentName;
    private String barcode;
    private String studentName;
    private String monitorName;
    private LocalDateTime loanDateTime;
    private LocalDateTime returnDateTime;
    private LoanStatus status;
}
