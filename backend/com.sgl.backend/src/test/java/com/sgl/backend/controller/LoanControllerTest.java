package com.sgl.backend.controller;

import com.sgl.backend.dto.LoanRequest;
import com.sgl.backend.dto.LoanResponse;
import com.sgl.backend.service.LoanService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanControllerTest {

    @Mock
    private LoanService loanService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private LoanController loanController;

    private LoanRequest request;
    private LoanResponse response;

    @BeforeEach
    void setup() {
        request = LoanRequest.builder()
                .barcode("EQ123")
                .studentCode("S001")
                .monitorCode("M001")
                .build();

        response = LoanResponse.builder()
                .id(1L)
                .equipmentName("Camera")
                .barcode("EQ123")
                .studentName("Student 1")
                .monitorName("Monitor 1")
                .loanDateTime(LocalDateTime.now())
                .status(com.sgl.backend.entity.Loan.LoanStatus.ACTIVE)
                .build();
    }

    @Test
    void registerLoan_success() {
        when(loanService.registerLoan(request)).thenReturn(response);

        ResponseEntity<LoanResponse> result = loanController.register(request);

        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals("Camera", result.getBody().getEquipmentName());
        verify(loanService).registerLoan(request);
    }

    @Test
    void returnLoan_success() {
        when(authentication.getName()).thenReturn("M001");
        when(loanService.returnLoan(1L, "M001")).thenReturn(response);

        ResponseEntity<LoanResponse> result = loanController.returnLoan(1L, authentication);

        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals("Camera", result.getBody().getEquipmentName());
        verify(loanService).returnLoan(1L, "M001");
    }

    @Test
    void getMyActiveLoans_success() {
        when(authentication.getName()).thenReturn("S001");
        when(loanService.getMyActiveLoans("S001")).thenReturn(List.of(response));

        ResponseEntity<List<LoanResponse>> result = loanController.getMyActive(authentication);

        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().size());
        assertEquals("Camera", result.getBody().get(0).getEquipmentName());
        verify(loanService).getMyActiveLoans("S001");
    }

    @Test
    void getAllActiveLoans_success() {
        when(loanService.getAllActiveLoans()).thenReturn(List.of(response));

        ResponseEntity<List<LoanResponse>> result = loanController.getAllActive();

        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().size());
        assertEquals("Camera", result.getBody().get(0).getEquipmentName());
        verify(loanService).getAllActiveLoans();
    }
}
