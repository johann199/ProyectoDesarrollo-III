package com.sgl.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sgl.backend.dto.LoanRequest;
import com.sgl.backend.dto.LoanResponse;
import com.sgl.backend.entity.Equipment;
import com.sgl.backend.entity.Equipment.EquipmentStatus;
import com.sgl.backend.entity.Loan;
import com.sgl.backend.entity.Loan.LoanStatus;
import com.sgl.backend.entity.User;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.repository.EquipmentRepository;
import com.sgl.backend.repository.LoanRepository;
import com.sgl.backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanService {

        private final LoanRepository loanRepo;
        private final EquipmentRepository equipmentRepo;
        private final UserRepository userRepo;

        public LoanResponse registerLoan(LoanRequest request) {
                User monitor = validateMonitor(request.getMonitorCode());
                User student = validateStudent(request.getStudentCode());
                Equipment equipment = validateEquipmentByBarcode(request.getBarcode());

                if (equipment.getAvailableUnits() <= 0) {
                        throw new SglException(
                                        "Equipment not found: '" + equipment.getName()
                                                        + "' has no available units",
                                        HttpStatus.CONFLICT);
                }

                Loan loan = Loan.builder()
                                .equipment(equipment)
                                .student(student)
                                .monitor(monitor)
                                .loanDateTime(LocalDateTime.now())
                                .status(LoanStatus.ACTIVE)
                                .build();

                loanRepo.save(loan);

                equipment.setAvailableUnits(equipment.getAvailableUnits() - 1);
                if (equipment.getAvailableUnits() == 0) {
                        equipment.setStatus(EquipmentStatus.LOANED);
                }
                equipmentRepo.save(equipment);

                return buildLoanResponse(loan);
        }

        public LoanResponse returnLoan(Long loanId, String monitorCode) {
                validateMonitor(monitorCode);

                Loan loan = loanRepo.findById(loanId)
                                .orElseThrow(() -> new SglException("Loan not found: " + loanId,
                                                HttpStatus.NOT_FOUND));

                if (loan.getStatus() == LoanStatus.RETURNED) {
                        throw new SglException("This loan has already been returned", HttpStatus.BAD_REQUEST);
                }

                loan.setStatus(LoanStatus.RETURNED);
                loan.setReturnDateTime(LocalDateTime.now());
                loanRepo.save(loan);

                Equipment equipment = loan.getEquipment();
                equipment.setAvailableUnits(equipment.getAvailableUnits() + 1);
                equipment.setStatus(EquipmentStatus.AVAILABLE);
                equipmentRepo.save(equipment);

                return buildLoanResponse(loan);
        }

        public List<LoanResponse> getMyActiveLoans(String studentCode) {
                validateStudentExists(studentCode);
                return loanRepo.findByStudentCodeAndStatus(studentCode, LoanStatus.ACTIVE).stream()
                                .map(this::buildLoanResponse)
                                .toList();
        }

        public List<LoanResponse> getAllActiveLoans() {
                return loanRepo.findByStatus(LoanStatus.ACTIVE).stream()
                                .map(this::buildLoanResponse)
                                .toList();
        }

        private User validateMonitor(String code) {
                User user = userRepo.findById(code)
                                .orElseThrow(() -> new SglException("Monitor not found: " + code,
                                                HttpStatus.NOT_FOUND));
                if (!user.getRole().getName().equalsIgnoreCase("MONITOR")) {
                        throw new SglException("Only monitors can perform this action", HttpStatus.FORBIDDEN);
                }
                return user;
        }

        private User validateStudent(String code) {
                User user = userRepo.findById(code)
                                .orElseThrow(() -> new SglException("Student not found: " + code,
                                                HttpStatus.NOT_FOUND));
                if (!user.getRole().getName().equalsIgnoreCase("ESTUDIANTE")) {
                        throw new SglException("Code does not belong to a student", HttpStatus.BAD_REQUEST);
                }
                return user;
        }

        private void validateStudentExists(String code) {
                if (!userRepo.existsByCodeAndRole_Name(code, "ESTUDIANTE")) {
                        throw new SglException("Student not found", HttpStatus.NOT_FOUND);
                }
        }

        private Equipment validateEquipmentByBarcode(String barcode) {
                return equipmentRepo.findByBarcode(barcode)
                                .orElseThrow(() -> new SglException(
                                                "Equipment not found with barcode: " + barcode, HttpStatus.NOT_FOUND));
        }

        private LoanResponse buildLoanResponse(Loan loan) {
                return LoanResponse.builder()
                                .id(loan.getId())
                                .equipmentName(loan.getEquipment().getName())
                                .barcode(loan.getEquipment().getBarcode())
                                .studentName(loan.getStudent().getName())
                                .monitorName(loan.getMonitor().getName())
                                .loanDateTime(loan.getLoanDateTime())
                                .returnDateTime(loan.getReturnDateTime())
                                .status(loan.getStatus())
                                .build();
        }
}