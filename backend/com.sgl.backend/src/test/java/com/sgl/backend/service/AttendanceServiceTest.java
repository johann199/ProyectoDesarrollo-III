package com.sgl.backend.service;

import com.sgl.backend.entity.Attendance;
import com.sgl.backend.entity.Role;
import com.sgl.backend.entity.User;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.dto.AttendanceReportDTO;
import com.sgl.backend.dto.AttendanceSummaryDTO;
import com.sgl.backend.dto.OpacUserInfo;
import com.sgl.backend.repository.AttendanceRepository;
import com.sgl.backend.repository.RoleRepository;
import com.sgl.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OpacService opacService;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void registerAttendance_existingUser_success() {
        User user = User.builder().code("12345").role(Role.builder().name("ESTUDIANTE").build()).build();
        when(userRepository.findById("12345")).thenReturn(Optional.of(user));
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        when(attendanceRepository.findByUserAndTimestampBetween(user, startOfDay, endOfDay))
                .thenReturn(Optional.empty());
        Attendance attendance = Attendance.builder().id(1L).user(user).timestamp(LocalDateTime.now()).build();
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        Attendance result = attendanceService.registerAttendance("12345");

        assertThat(result.getUser().getCode()).isEqualTo("12345");
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void registerAttendance_newUserFromOpac_success() {
        OpacUserInfo opacInfo = OpacUserInfo.builder().code("12345").name("John Doe").program("Ingeniería").build();
        Role role = Role.builder().id(1L).name("ESTUDIANTE").build();
        User user = User.builder().code("12345").name("John Doe").role(role).build();
        when(userRepository.findById("12345")).thenReturn(Optional.empty());
        when(opacService.fetchUserInfo("12345")).thenReturn(opacInfo);
        when(roleRepository.findByName("ESTUDIANTE")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        when(attendanceRepository.findByUserAndTimestampBetween(any(User.class), eq(startOfDay), eq(endOfDay)))
                .thenReturn(Optional.empty());
        Attendance attendance = Attendance.builder().id(1L).user(user).timestamp(LocalDateTime.now()).build();
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        Attendance result = attendanceService.registerAttendance("12345");

        assertThat(result.getUser().getCode()).isEqualTo("12345");
        verify(userRepository).save(any(User.class));
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void registerAttendance_duplicate_throwsException() {
        User user = User.builder().code("12345").role(Role.builder().name("ESTUDIANTE").build()).build();
        when(userRepository.findById("12345")).thenReturn(Optional.of(user));
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        when(attendanceRepository.findByUserAndTimestampBetween(user, startOfDay, endOfDay))
                .thenReturn(Optional.of(Attendance.builder().build()));

        assertThrows(SglException.class, () -> attendanceService.registerAttendance("12345"),
                "Attendance already registered for user 12345 today");
    }

    @Test
    void getAttendances_byUserCode_success() {
        User user = User.builder().code("12345").build();
        Attendance attendance = Attendance.builder().id(1L).user(user).timestamp(LocalDateTime.now()).build();
        when(attendanceRepository.findByUserCode("12345")).thenReturn(List.of(attendance));

        List<Attendance> result = attendanceService.getAttendances(null, null, "12345");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getCode()).isEqualTo("12345");
    }

    @Test
    void getAttendances_byDateRange_success() {
        User user = User.builder().code("12345").build();
        Attendance attendance = Attendance.builder().id(1L).user(user).timestamp(LocalDateTime.now()).build();
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        when(attendanceRepository.findByTimestampBetween(start, end)).thenReturn(List.of(attendance));

        List<Attendance> result = attendanceService.getAttendances(start, end, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAttendancesPerMonth_success() {
        when(attendanceRepository.countByYearAndMonth(2024, 1)).thenReturn(5);
        when(attendanceRepository.countByYearAndMonth(2024, 2)).thenReturn(3);

        for (int i = 3; i <= 12; i++) {
            when(attendanceRepository.countByYearAndMonth(2024, i)).thenReturn(0);
        }

        int[] result = attendanceService.getAttendancesPerMonth(2024);

        assertThat(result[0]).isEqualTo(5);
        assertThat(result[1]).isEqualTo(3);
        assertThat(result[2]).isEqualTo(0);
        assertThat(result).hasSize(12);

        verify(attendanceRepository, times(12)).countByYearAndMonth(eq(2024), anyInt());
    }

    @Test
    void getMonthlySummary_success() {
        User u1 = User.builder().code("A").build();
        User u2 = User.builder().code("B").build();

        Attendance a1 = Attendance.builder()
                .user(u1)
                .timestamp(LocalDateTime.of(2024, 2, 5, 10, 0))
                .build();

        Attendance a2 = Attendance.builder()
                .user(u2)
                .timestamp(LocalDateTime.of(2024, 2, 10, 12, 0))
                .build();

        Page<Attendance> page = new PageImpl<>(List.of(a1, a2));

        when(attendanceRepository.findByYearAndMonth(2024, 2, Pageable.unpaged()))
                .thenReturn(page);

        AttendanceSummaryDTO summary = attendanceService.getMonthlySummary(2024, 2);

        assertThat(summary.getTotalUsers()).isEqualTo(2);
        assertThat(summary.getTotalAttendances()).isEqualTo(2);
        assertThat(summary.getFirstAttendance()).isEqualTo(LocalDate.of(2024, 2, 5));
        assertThat(summary.getLastAttendance()).isEqualTo(LocalDate.of(2024, 2, 10));
    }

    @Test
    void getMonthlySummary_empty() {
        Page<Attendance> emptyPage = Page.empty();

        when(attendanceRepository.findByYearAndMonth(2024, 3, Pageable.unpaged()))
                .thenReturn(emptyPage);

        AttendanceSummaryDTO summary = attendanceService.getMonthlySummary(2024, 3);

        assertThat(summary.getTotalUsers()).isEqualTo(0);
        assertThat(summary.getTotalAttendances()).isEqualTo(0);
    }

    @Test
    void getMonthlyReport_success() {
        User user = User.builder()
                .code("123")
                .name("John")
                .role(Role.builder().name("STUDENT").build())
                .build();

        Attendance att = Attendance.builder()
                .timestamp(LocalDateTime.of(2024, 2, 10, 10, 0))
                .user(user)
                .build();

        Page<Attendance> page = new org.springframework.data.domain.PageImpl<>(List.of(att));

        when(attendanceRepository.findByYearAndMonth(2024, 2, Pageable.unpaged()))
                .thenReturn(page);

        when(attendanceRepository.countByUserAndYearMonth("123", 2024, 2))
                .thenReturn(3L);

        when(attendanceRepository.findByUserCode("123"))
                .thenReturn(List.of(att));

        Page<AttendanceReportDTO> result = attendanceService.getMonthlyReport(2024, 2, Pageable.unpaged());

        AttendanceReportDTO dto = result.getContent().get(0);

        assertThat(dto.getUserCode()).isEqualTo("123");
        assertThat(dto.getUserName()).isEqualTo("John");
        assertThat(dto.getTotalAttendances()).isEqualTo(3);
        assertThat(dto.getAttendanceDates()).contains(LocalDate.of(2024, 2, 10));
    }

    @Test
    void generateAttendanceCsv_success() {
        AttendanceReportDTO dto = AttendanceReportDTO.builder()
                .userCode("123")
                .userName("John")
                .userRole("STUDENT")
                .totalAttendances(5)
                .build();

        when(attendanceRepository.countByYearAndMonth(2024, 1)).thenReturn(2);
        for (int i = 2; i <= 12; i++) {
            when(attendanceRepository.countByYearAndMonth(2024, i)).thenReturn(0);
        }

        byte[] csvBytes = attendanceService.generateAttendanceCsv(List.of(dto), 2024);

        String csv = new String(csvBytes);

        assertThat(csv).contains("123");
        assertThat(csv).contains("John");
        assertThat(csv).contains("STUDENT");
        assertThat(csv).contains("5");
        assertThat(csv).contains("Mes,Asistencias");
    }

    @Test
    void generateAttendancePdf_success() {
        AttendanceReportDTO dto = AttendanceReportDTO.builder()
                .userCode("123")
                .userName("John")
                .userRole("STUDENT")
                .totalAttendances(5)
                .build();

        when(attendanceRepository.countByYearAndMonth(anyInt(), anyInt())).thenReturn(1);

        byte[] pdfBytes = attendanceService.generateAttendancePdf(List.of(dto), 2024, 2);

        assertThat(pdfBytes).isNotEmpty();
    }
}