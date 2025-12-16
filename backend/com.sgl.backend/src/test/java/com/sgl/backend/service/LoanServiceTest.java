package com.sgl.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import com.sgl.backend.dto.LoanRequest;
import com.sgl.backend.dto.LoanResponse;
import com.sgl.backend.entity.Equipment;
import com.sgl.backend.entity.Loan;
import com.sgl.backend.entity.Role;
import com.sgl.backend.entity.User;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.repository.EquipmentRepository;
import com.sgl.backend.repository.LoanRepository;
import com.sgl.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepo;
    @Mock
    private EquipmentRepository equipmentRepo;
    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private LoanService loanService;

    private User monitor;
    private User student;
    private Equipment equipment;
    private LoanRequest request;

    @BeforeEach
    void setup() {
        monitor = User.builder()
                .code("M001")
                .name("Monitor 1")
                .role(Role.builder().id(2L).name("MONITOR").build())
                .build();

        student = User.builder()
                .code("S001")
                .name("Student 1")
                .role(Role.builder().id(3L).name("ESTUDIANTE").build())
                .build();

        equipment = Equipment.builder()
                .id(10L)
                .name("Camera")
                .barcode("EQ123")
                .availableUnits(2)
                .status(Equipment.EquipmentStatus.AVAILABLE)
                .build();

        request = LoanRequest.builder()
                .monitorCode(monitor.getCode())
                .studentCode(student.getCode())
                .barcode(equipment.getBarcode())
                .build();
    }

    @Test
    void registerLoan_successful() {
        when(userRepo.findById(monitor.getCode())).thenReturn(Optional.of(monitor));
        when(userRepo.findById(student.getCode())).thenReturn(Optional.of(student));
        when(equipmentRepo.findByBarcode(equipment.getBarcode())).thenReturn(Optional.of(equipment));

        Loan loan = Loan.builder()
                .id(99L)
                .equipment(equipment)
                .student(student)
                .monitor(monitor)
                .loanDateTime(LocalDateTime.now())
                .status(Loan.LoanStatus.ACTIVE)
                .build();

        when(loanRepo.save(any(Loan.class))).thenReturn(loan);
        when(equipmentRepo.save(any(Equipment.class))).thenReturn(equipment);

        LoanResponse response = loanService.registerLoan(request);

        assertNotNull(response);
        assertEquals("Camera", response.getEquipmentName());
        assertEquals("Student 1", response.getStudentName());
        assertEquals("Monitor 1", response.getMonitorName());
        assertEquals(Loan.LoanStatus.ACTIVE, response.getStatus());

        verify(loanRepo).save(any(Loan.class));
        verify(equipmentRepo).save(any(Equipment.class));
    }

    @Test
    void registerLoan_monitorNotFound() {
        when(userRepo.findById(monitor.getCode())).thenReturn(Optional.empty());

        SglException ex = assertThrows(SglException.class, () -> loanService.registerLoan(request));
        assertTrue(ex.getMessage().contains("Monitor not found"));
    }

    @Test
    void registerLoan_monitorNotAuthorized() {
        monitor.setRole(Role.builder().id(1L).name("ADMIN").build());
        when(userRepo.findById(monitor.getCode())).thenReturn(Optional.of(monitor));

        SglException ex = assertThrows(SglException.class, () -> loanService.registerLoan(request));
        assertTrue(ex.getMessage().contains("Only monitors can perform this action"));
    }

    @Test
    void registerLoan_studentNotFound() {
        when(userRepo.findById(monitor.getCode())).thenReturn(Optional.of(monitor));
        when(userRepo.findById(student.getCode())).thenReturn(Optional.empty());

        SglException ex = assertThrows(SglException.class, () -> loanService.registerLoan(request));
        assertTrue(ex.getMessage().contains("Student not found"));
    }

    @Test
    void registerLoan_invalidStudentRole() {
        student.setRole(Role.builder().id(4L).name("GUEST").build());
        when(userRepo.findById(monitor.getCode())).thenReturn(Optional.of(monitor));
        when(userRepo.findById(student.getCode())).thenReturn(Optional.of(student));

        SglException ex = assertThrows(SglException.class, () -> loanService.registerLoan(request));
        assertTrue(ex.getMessage().contains("does not belong to a student"));
    }

    @Test
    void registerLoan_equipmentNotFound() {
        when(userRepo.findById(monitor.getCode())).thenReturn(Optional.of(monitor));
        when(userRepo.findById(student.getCode())).thenReturn(Optional.of(student));
        when(equipmentRepo.findByBarcode(equipment.getBarcode())).thenReturn(Optional.empty());

        SglException ex = assertThrows(SglException.class, () -> loanService.registerLoan(request));
        assertTrue(ex.getMessage().contains("Equipment not found"));
    }

    @Test
    void registerLoan_equipmentNotAvailable() {
        equipment.setAvailableUnits(0);
        when(userRepo.findById(monitor.getCode())).thenReturn(Optional.of(monitor));
        when(userRepo.findById(student.getCode())).thenReturn(Optional.of(student));
        when(equipmentRepo.findByBarcode(equipment.getBarcode())).thenReturn(Optional.of(equipment));

        SglException ex = assertThrows(SglException.class, () -> loanService.registerLoan(request));
        assertTrue(ex.getMessage().contains("has no available units"));
    }

    @Test
    void returnLoan_successful() {
        Loan loan = Loan.builder()
                .id(100L)
                .equipment(equipment)
                .student(student)
                .monitor(monitor)
                .status(Loan.LoanStatus.ACTIVE)
                .loanDateTime(LocalDateTime.now())
                .build();

        when(userRepo.findById(monitor.getCode())).thenReturn(Optional.of(monitor));
        when(loanRepo.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(loanRepo.save(any(Loan.class))).thenReturn(loan);
        when(equipmentRepo.save(any(Equipment.class))).thenReturn(equipment);

        LoanResponse response = loanService.returnLoan(loan.getId(), monitor.getCode());

        assertNotNull(response);
        assertEquals(Loan.LoanStatus.RETURNED, response.getStatus());
        assertNotNull(response.getReturnDateTime());

        verify(loanRepo).save(any(Loan.class));
        verify(equipmentRepo).save(any(Equipment.class));
    }

    @Test
    void returnLoan_monitorNotAuthorized() {
        User notMonitor = User.builder()
                .code("U001")
                .name("User 1")
                .role(Role.builder().name("ESTUDIANTE").build())
                .build();

        when(userRepo.findById(notMonitor.getCode())).thenReturn(Optional.of(notMonitor));

        SglException ex = assertThrows(SglException.class,
                () -> loanService.returnLoan(1L, notMonitor.getCode()));

        assertTrue(ex.getMessage().contains("Only monitors can perform this action"));
    }

    @Test
    void returnLoan_loanNotFound() {
        when(userRepo.findById(monitor.getCode())).thenReturn(Optional.of(monitor));
        when(loanRepo.findById(1L)).thenReturn(Optional.empty());

        SglException ex = assertThrows(SglException.class,
                () -> loanService.returnLoan(1L, monitor.getCode()));

        assertTrue(ex.getMessage().contains("Loan not found"));
    }

    @Test
    void returnLoan_alreadyReturned() {
        Loan loan = Loan.builder()
                .id(200L)
                .equipment(equipment)
                .student(student)
                .monitor(monitor)
                .status(Loan.LoanStatus.RETURNED)
                .build();

        when(userRepo.findById(monitor.getCode())).thenReturn(Optional.of(monitor));
        when(loanRepo.findById(loan.getId())).thenReturn(Optional.of(loan));

        SglException ex = assertThrows(SglException.class,
                () -> loanService.returnLoan(loan.getId(), monitor.getCode()));

        assertTrue(ex.getMessage().contains("already been returned"));
    }

    @Test
    void getMyActiveLoans_successful() {
        Loan loan = Loan.builder()
                .id(1L)
                .equipment(equipment)
                .student(student)
                .monitor(monitor)
                .status(Loan.LoanStatus.ACTIVE)
                .loanDateTime(LocalDateTime.now())
                .build();

        when(userRepo.existsByCodeAndRole_Name(eq(student.getCode()), eq("ESTUDIANTE"))).thenReturn(true);
        when(loanRepo.findByStudentCodeAndStatus(student.getCode(), Loan.LoanStatus.ACTIVE))
                .thenReturn(List.of(loan));

        var result = loanService.getMyActiveLoans(student.getCode());

        assertEquals(1, result.size());
        assertEquals("Camera", result.get(0).getEquipmentName());
        verify(loanRepo).findByStudentCodeAndStatus(student.getCode(), Loan.LoanStatus.ACTIVE);
    }

    @Test
    void getMyActiveLoans_studentNotFound() {
        when(userRepo.existsByCodeAndRole_Name(eq(student.getCode()), eq("ESTUDIANTE"))).thenReturn(false);

        SglException ex = assertThrows(SglException.class,
                () -> loanService.getMyActiveLoans(student.getCode()));

        assertTrue(ex.getMessage().contains("Student not found"));
    }

    @Test
    void getAllActiveLoans_successful() {
        Loan loan1 = Loan.builder()
                .id(1L)
                .equipment(equipment)
                .student(student)
                .monitor(monitor)
                .status(Loan.LoanStatus.ACTIVE)
                .loanDateTime(LocalDateTime.now())
                .build();

        when(loanRepo.findByStatus(Loan.LoanStatus.ACTIVE)).thenReturn(List.of(loan1));

        var result = loanService.getAllActiveLoans();

        assertEquals(1, result.size());
        assertEquals(Loan.LoanStatus.ACTIVE, result.get(0).getStatus());
        verify(loanRepo).findByStatus(Loan.LoanStatus.ACTIVE);
    }
}
