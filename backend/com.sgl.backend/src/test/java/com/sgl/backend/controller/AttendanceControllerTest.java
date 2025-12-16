package com.sgl.backend.controller;

import com.sgl.backend.dto.AttendanceReportDTO;
import com.sgl.backend.dto.AttendanceSummaryDTO;
import com.sgl.backend.entity.Attendance;
import com.sgl.backend.entity.User;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.security.JwtAuthenticationFilter;
import com.sgl.backend.security.JwtService;
import com.sgl.backend.service.AttendanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(AttendanceController.class)
@AutoConfigureMockMvc(addFilters = false)
class AttendanceControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private AttendanceService attendanceService;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @Test
        @WithMockUser(authorities = "ADMIN")
        void registerAttendance_success() throws Exception {
                User user = User.builder().code("12345").build();
                Attendance attendance = Attendance.builder().id(1L).user(user).timestamp(LocalDateTime.now()).build();
                when(attendanceService.registerAttendance("12345")).thenReturn(attendance);

                mockMvc.perform(post("/api/attendances/12345"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.user.code").value("12345"));
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void registerAttendance_duplicate_returns400() throws Exception {
                when(attendanceService.registerAttendance("12345"))
                                .thenThrow(new SglException("Attendance already registered for user 12345 today"));

                mockMvc.perform(post("/api/attendances/12345"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message")
                                                .value("Attendance already registered for user 12345 today"));
        }

        @Test
        @WithMockUser(authorities = "MONITOR")
        void getAttendances_byUserCode_success() throws Exception {
                User user = User.builder().code("12345").build();
                Attendance attendance = Attendance.builder().id(1L).user(user).timestamp(LocalDateTime.now()).build();
                when(attendanceService.getAttendances(null, null, "12345")).thenReturn(List.of(attendance));

                mockMvc.perform(get("/api/attendances").param("userCode", "12345"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].user.code").value("12345"));
        }

        @Test
        @WithMockUser(authorities = "MONITOR")
        void getAttendances_byDateRange_success() throws Exception {
                User user = User.builder().code("12345").build();
                Attendance attendance = Attendance.builder().id(1L).user(user).timestamp(LocalDateTime.now()).build();
                LocalDateTime start = LocalDate.now().atStartOfDay();
                LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
                when(attendanceService.getAttendances(any(LocalDateTime.class), any(LocalDateTime.class), eq(null)))
                                .thenReturn(List.of(attendance));

                mockMvc.perform(get("/api/attendances")
                                .param("start", start.toString())
                                .param("end", end.toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].user.code").value("12345"));
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void getMonthlyReport_success() throws Exception {
                AttendanceReportDTO dto = AttendanceReportDTO.builder()
                                .userCode("123")
                                .userName("John")
                                .userRole("STUDENT")
                                .totalAttendances(5)
                                .build();

                when(attendanceService.getMonthlyReport(2024, 1,
                                PageRequest.of(0, 10, Sort.by("user.name").ascending())))
                                .thenReturn(new PageImpl<>(List.of(dto)));

                mockMvc.perform(get("/api/attendances/report")
                                .param("year", "2024")
                                .param("month", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].userCode").value("123"))
                                .andExpect(jsonPath("$.content[0].userName").value("John"));
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void getMonthlySummary_success() throws Exception {
                AttendanceSummaryDTO summary = AttendanceSummaryDTO.builder()
                                .totalUsers(10)
                                .totalAttendances(50)
                                .build();

                when(attendanceService.getMonthlySummary(2024, 1)).thenReturn(summary);

                mockMvc.perform(get("/api/attendances/report/summary")
                                .param("year", "2024")
                                .param("month", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalUsers").value(10))
                                .andExpect(jsonPath("$.totalAttendances").value(50));
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void exportPdf_success() throws Exception {
                AttendanceReportDTO dto = AttendanceReportDTO.builder()
                                .userCode("123")
                                .userName("John")
                                .userRole("STUDENT")
                                .totalAttendances(5)
                                .build();

                when(attendanceService.getMonthlyReport(2024, 1, Pageable.unpaged()))
                                .thenReturn(new PageImpl<>(List.of(dto)));

                when(attendanceService.generateAttendancePdf(any(), eq(2024), eq(1)))
                                .thenReturn(new byte[] { 1, 2, 3 });

                mockMvc.perform(get("/api/attendances/report/pdf")
                                .param("year", "2024")
                                .param("month", "1"))
                                .andExpect(status().isOk())
                                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=reporte_asistencia_2024_01.pdf"))
                                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        void exportCsv_success() throws Exception {
                AttendanceReportDTO dto = AttendanceReportDTO.builder()
                                .userCode("123")
                                .userName("John")
                                .userRole("STUDENT")
                                .totalAttendances(5)
                                .build();

                when(attendanceService.getMonthlyReport(2024, 1, Pageable.unpaged()))
                                .thenReturn(new PageImpl<>(List.of(dto)));

                when(attendanceService.generateAttendanceCsv(any(), eq(2024)))
                                .thenReturn("CSV_DATA".getBytes());

                mockMvc.perform(get("/api/attendances/report/csv")
                                .param("year", "2024")
                                .param("month", "1"))
                                .andExpect(status().isOk())
                                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=reporte_asistencia_2024_01.csv"))
                                .andExpect(content().contentType("text/csv"));
        }
}