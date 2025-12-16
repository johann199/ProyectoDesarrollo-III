package com.sgl.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sgl.backend.entity.Loan;
import com.sgl.backend.entity.Loan.LoanStatus;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByStudentCodeAndStatus(String studentCode, LoanStatus status);
    List<Loan> findByStatus(LoanStatus status);
    boolean existsByIdAndStatus(Long id, LoanStatus status);
}
