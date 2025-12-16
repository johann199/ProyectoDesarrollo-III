package com.sgl.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sgl.backend.dto.LoanRequest;
import com.sgl.backend.dto.LoanResponse;
import com.sgl.backend.service.LoanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Loan Management", description = "Endpoints for managing equipment loans and returns")
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    @Operation(summary = "Register a new loan")
    public ResponseEntity<LoanResponse> register(@Valid @RequestBody LoanRequest request) {
        return ResponseEntity.ok(loanService.registerLoan(request));
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Return equipment")
    public ResponseEntity<LoanResponse> returnLoan(
            @PathVariable Long id,
            Authentication authentication) {
        String monitorCode = authentication.getName();
        return ResponseEntity.ok(loanService.returnLoan(id, monitorCode));
    }

    @GetMapping("/my-active")
    @Operation(summary = "My active loans")
    public ResponseEntity<List<LoanResponse>> getMyActive(Authentication authentication) {
        String studentCode = authentication.getName();
        return ResponseEntity.ok(loanService.getMyActiveLoans(studentCode));
    }

    @GetMapping("/active")
    @Operation(summary = "All active loans")
    public ResponseEntity<List<LoanResponse>> getAllActive() {
        return ResponseEntity.ok(loanService.getAllActiveLoans());
    }
}
