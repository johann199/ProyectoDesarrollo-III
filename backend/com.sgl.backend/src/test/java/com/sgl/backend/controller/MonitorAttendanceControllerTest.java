package com.sgl.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgl.backend.dto.AttendanceRequest;
import com.sgl.backend.dto.AttendanceRequest.AttendanceType;
import com.sgl.backend.dto.AttendanceResponse;
import com.sgl.backend.service.MonitorAttendanceService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

class MonitorAttendanceControllerTest {

        private MockMvc mockMvc;

        @Mock
        private MonitorAttendanceService service;

        private ObjectMapper mapper = new ObjectMapper();

        @BeforeEach
        void setup() {
                MockitoAnnotations.openMocks(this);

                MonitorAttendanceController controller = new MonitorAttendanceController(service);

                mockMvc = MockMvcBuilders.standaloneSetup(controller)
                                .setConversionService(new DefaultFormattingConversionService())
                                .build();
        }

        @Test
        void testRegister() throws Exception {

                AttendanceRequest req = new AttendanceRequest();
                req.setMonitorCode("M001");
                req.setType(AttendanceType.CHECK_IN);

                AttendanceResponse resp = AttendanceResponse.builder()
                                .id(1L)
                                .monitorCode("M001")
                                .monitorName("Juan")
                                .date(LocalDate.now())
                                .checkIn(LocalDateTime.now())
                                .build();

                when(service.registerAttendance(any())).thenReturn(resp);

                mockMvc.perform(post("/api/monitor-attendance")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.monitorCode").value("M001"));
        }

        @Test
        void testGetReportPdf() throws Exception {

                byte[] pdf = new byte[] { 1, 2, 3 };

                when(service.getReport(any(), any(), any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of()));

                when(service.generateMonitorReportPdf(anyList(), any(), any()))
                                .thenReturn(pdf);

                mockMvc.perform(get("/api/monitor-attendance/report/pdf")
                                .param("start", "2024-01-01")
                                .param("end", "2024-01-31"))
                                .andExpect(status().isOk())
                                .andExpect(header().exists("Content-Disposition"))
                                .andExpect(content().bytes(pdf));
        }

        @Test
        void testGetReportCsv() throws Exception {

                byte[] csv = "Código,Nombre\nM001,Juan".getBytes();

                when(service.getReport(any(), any(), any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of()));

                when(service.generateMonitorReportCsv(anyList()))
                                .thenReturn(csv);

                mockMvc.perform(get("/api/monitor-attendance/report/csv")
                                .param("start", "2024-01-01")
                                .param("end", "2024-01-31"))
                                .andExpect(status().isOk())
                                .andExpect(header().exists("Content-Disposition"))
                                .andExpect(content().bytes(csv));
        }
}
