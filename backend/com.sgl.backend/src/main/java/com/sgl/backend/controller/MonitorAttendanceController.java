package com.sgl.backend.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sgl.backend.dto.AttendanceRequest;
import com.sgl.backend.dto.AttendanceResponse;
import com.sgl.backend.dto.MonitorReportResponse;
import com.sgl.backend.service.MonitorAttendanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/monitor-attendance")
@RequiredArgsConstructor
@Tag(name = "Monitor Attendance")
public class MonitorAttendanceController {

        private final MonitorAttendanceService service;

        @PostMapping
        @Operation(summary = "Register check-in/check-out")
        public ResponseEntity<AttendanceResponse> register(@Valid @RequestBody AttendanceRequest request) {
                return ResponseEntity.ok(service.registerAttendance(request));
        }

        @GetMapping("report")
        @Operation(summary = "Attendance report", description = "Admin only")
        public ResponseEntity<Page<MonitorReportResponse>> getReport(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

                Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

                return ResponseEntity.ok(service.getReport(start, end, pageable));
        }

        @GetMapping(value = "/report/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
        @Operation(summary = "PDF report")
        public ResponseEntity<byte[]> getReportPdf(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

                Page<MonitorReportResponse> page = service.getReport(start, end, Pageable.unpaged());
                byte[] pdf = service.generateMonitorReportPdf(page.getContent(), start, end);

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=reporte_asistencia_" + start + "_" + end + ".pdf")
                                .body(pdf);
        }

        @GetMapping(value = "/report/csv", produces = "text/csv")
        @Operation(summary = "CSV report")
        public ResponseEntity<byte[]> getReportCsv(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

                Page<MonitorReportResponse> page = service.getReport(start, end, Pageable.unpaged());
                byte[] csv = service.generateMonitorReportCsv(page.getContent());

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=reporte_asistencia_" + start + "_" + end + ".csv")
                                .body(csv);
        }
}
