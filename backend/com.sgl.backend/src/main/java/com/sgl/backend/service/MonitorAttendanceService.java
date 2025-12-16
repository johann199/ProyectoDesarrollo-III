package com.sgl.backend.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sgl.backend.dto.AttendanceRequest;
import com.sgl.backend.dto.AttendanceResponse;
import com.sgl.backend.dto.MonitorReportResponse;
import com.sgl.backend.dto.AttendanceRequest.AttendanceType;
import com.sgl.backend.entity.MonitorAttendance;
import com.sgl.backend.entity.Role;
import com.sgl.backend.entity.User;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.repository.MonitorAttendanceRepository;
import com.sgl.backend.repository.RoleRepository;
import com.sgl.backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MonitorAttendanceService {

    private final MonitorAttendanceRepository attendanceRepo;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    public AttendanceResponse registerAttendance(AttendanceRequest request) {
        User monitor = validateMonitor(request.getMonitorCode());

        LocalDate today = LocalDate.now();

        MonitorAttendance lastRecord = attendanceRepo
                .findFirstByMonitorCodeAndDateOrderByCheckInDesc(request.getMonitorCode(), today)
                .orElse(null);

        MonitorAttendance record;

        if (request.getType() == AttendanceType.CHECK_IN) {

            if (lastRecord == null || lastRecord.getCheckOut() != null) {
                record = MonitorAttendance.builder()
                        .monitor(monitor)
                        .date(today)
                        .checkIn(LocalDateTime.now())
                        .build();
            } else if (lastRecord.getCheckIn() != null && lastRecord.getCheckOut() == null) {
                throw new SglException("You already checked in and haven't checked out yet", HttpStatus.CONFLICT);
            } else {
                record = lastRecord;
            }

        } else {

            if (lastRecord == null || lastRecord.getCheckIn() == null) {
                throw new SglException("You cannot check out without checking in", HttpStatus.BAD_REQUEST);
            }

            if (lastRecord.getCheckOut() != null) {
                throw new SglException("You already checked out, do check-in again for next shift",
                        HttpStatus.CONFLICT);
            }

            record = lastRecord;
            record.setCheckOut(LocalDateTime.now());
        }

        attendanceRepo.save(record);
        return buildResponse(record);
    }

    public Page<MonitorReportResponse> getReport(LocalDate start, LocalDate end, Pageable pageable) {
        Role monitorRole = roleRepo.findByName("MONITOR")
                .orElseThrow(() -> new SglException("Monitor role not found", HttpStatus.NOT_FOUND));

        Page<User> monitorPage = userRepo.findByRole(monitorRole, pageable);

        return monitorPage.map(monitor -> {
            List<MonitorAttendance> records = attendanceRepo
                    .findByMonitorCodeAndDateBetween(monitor.getCode(), start, end);

            List<AttendanceResponse> attendances = records.stream()
                    .map(this::buildResponse)
                    .toList();

            Double totalHours = records.stream()
                    .filter(r -> r.getCheckIn() != null && r.getCheckOut() != null)
                    .mapToDouble(r -> Duration.between(r.getCheckIn(), r.getCheckOut()).toMinutes() / 60.0)
                    .sum();

            return MonitorReportResponse.builder()
                    .monitorCode(monitor.getCode())
                    .monitorName(monitor.getName())
                    .totalDaysWorked(
                        (int) records.stream()
                            .map(MonitorAttendance::getDate)
                            .distinct()
                            .count()
                    )
                    .totalHoursWorked(totalHours)
                    .attendances(attendances)
                    .build();
        });
    }

    public byte[] generateMonitorReportPdf(List<MonitorReportResponse> data, LocalDate start, LocalDate end) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("REPORTE DE ASISTENCIA DE MONITORES",
                    new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD)));
            document.add(new Paragraph("Período: " + start + " al " + end));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell("Código");
            table.addCell("Nombre");
            table.addCell("Días");
            table.addCell("Horas");

            for (MonitorReportResponse m : data) {
                table.addCell(m.getMonitorCode());
                table.addCell(m.getMonitorName());
                table.addCell(String.valueOf(m.getTotalDaysWorked()));
                table.addCell(String.format("%.2f", m.getTotalHoursWorked()));
            }

            document.add(table);

        } catch (Exception e) {
            throw new SglException("Error generating PDF", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    public byte[] generateMonitorReportCsv(List<MonitorReportResponse> data) {
        StringWriter writer = new StringWriter();
        CSVPrinter csvPrinter;

        try {
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader("Código", "Nombre", "Días Trabajados", "Horas Totales")
                    .build();
            csvPrinter = new CSVPrinter(writer, csvFormat);

            for (MonitorReportResponse m : data) {
                csvPrinter.printRecord(
                        m.getMonitorCode(),
                        m.getMonitorName(),
                        m.getTotalDaysWorked(),
                        String.format("%.2f", m.getTotalHoursWorked()));
            }

            csvPrinter.close();
            return writer.toString().getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SglException("Error generating CSV", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private User validateMonitor(String code) {
        User user = userRepo.findById(code)
                .orElseThrow(() -> new SglException("Monitor not found", HttpStatus.NOT_FOUND));
        if (!"MONITOR".equals(user.getRole().getName())) {
            throw new SglException("Access denies", HttpStatus.FORBIDDEN);
        }
        return user;
    }

    private AttendanceResponse buildResponse(MonitorAttendance ma) {
        Double hours = null;
        String status = "MISSING";

        if (ma.getCheckIn() != null && ma.getCheckOut() != null) {
            hours = Duration.between(ma.getCheckIn(), ma.getCheckOut()).toMinutes() / 60.0;
            status = "COMPLETED";
        } else if (ma.getCheckIn() != null) {
            status = "IN_PROGRESS";
        }

        return AttendanceResponse.builder()
                .id(ma.getId())
                .monitorName(ma.getMonitor().getName())
                .monitorCode(ma.getMonitor().getCode())
                .date(ma.getDate())
                .checkIn(ma.getCheckIn())
                .checkOut(ma.getCheckOut())
                .hoursWorked(hours)
                .status(status)
                .build();
    }
}
