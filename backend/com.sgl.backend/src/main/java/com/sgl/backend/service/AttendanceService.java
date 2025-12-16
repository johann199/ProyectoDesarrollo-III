package com.sgl.backend.service;

import com.sgl.backend.entity.Attendance;
import com.sgl.backend.entity.Role;
import com.sgl.backend.entity.User;
import com.sgl.backend.exception.SglException;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.util.Arrays;

import com.sgl.backend.dto.AttendanceReportDTO;
import com.sgl.backend.dto.AttendanceSummaryDTO;
import com.sgl.backend.dto.OpacUserInfo;
import com.sgl.backend.repository.AttendanceRepository;
import com.sgl.backend.repository.RoleRepository;
import com.sgl.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OpacService opacService;

    public Attendance registerAttendance(String code) {
        User user = userRepository.findById(code).orElse(null);
        if (user == null) {
            OpacUserInfo opacInfo = opacService.fetchUserInfo(code);
            Role role = roleRepository.findByName("ESTUDIANTE")
                    .orElseThrow(() -> new SglException("Default role ESTUDIANTE not found"));
            user = User.builder()
                    .code(opacInfo.getCode())
                    .name(opacInfo.getName())
                    .email(opacInfo.getEmail())
                    .document(opacInfo.getDocument())
                    .role(role)
                    .build();
            user = userRepository.save(user);
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        if (attendanceRepository.findByUserAndTimestampBetween(user, startOfDay, endOfDay).isPresent()) {
            throw new SglException("Attendance already registered for user " + code + " today");
        }

        Attendance attendance = Attendance.builder()
                .user(user)
                .timestamp(LocalDateTime.now())
                .build();
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getAttendances(LocalDateTime start, LocalDateTime end, String userCode) {
        if (userCode != null && !userCode.isEmpty()) {
            return attendanceRepository.findByUserCode(userCode);
        }
        if (start != null && end != null) {
            return attendanceRepository.findByTimestampBetween(start, end);
        }
        return attendanceRepository.findAll();
    }

    public Page<AttendanceReportDTO> getMonthlyReport(int year, int month, Pageable pageable) {
        Page<Attendance> page = attendanceRepository.findByYearAndMonth(year, month, pageable);

        return page.map(att -> {
            String code = att.getUser().getCode();
            long count = attendanceRepository.countByUserAndYearMonth(code, year, month);

            List<LocalDate> dates = attendanceRepository.findByUserCode(code).stream()
                    .filter(a -> a.getTimestamp().getYear() == year && a.getTimestamp().getMonthValue() == month)
                    .map(a -> a.getTimestamp().toLocalDate())
                    .distinct()
                    .sorted()
                    .toList();

            return AttendanceReportDTO.builder()
                    .userCode(code)
                    .userName(att.getUser().getName())
                    .userRole(att.getUser().getRole().getName())
                    .totalAttendances((int) count)
                    .attendanceDates(dates)
                    .build();
        });
    }

    public AttendanceSummaryDTO getMonthlySummary(int year, int month) {
        Page<Attendance> page = attendanceRepository.findByYearAndMonth(year, month, Pageable.unpaged());
        if (page.isEmpty()) {
            return AttendanceSummaryDTO.builder().totalUsers(0).totalAttendances(0).build();
        }

        Set<String> users = page.stream().map(a -> a.getUser().getCode()).collect(Collectors.toSet());

        return AttendanceSummaryDTO.builder()
                .totalUsers(users.size())
                .totalAttendances(page.getContent().size())
                .firstAttendance(page.getContent().get(0).getTimestamp().toLocalDate())
                .lastAttendance(page.getContent().get(page.getContent().size() - 1).getTimestamp().toLocalDate())
                .build();
    }

    public byte[] generateAttendancePdf(List<AttendanceReportDTO> data, int year, int month) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            document.add(new Paragraph("REPORTE DE ASISTENCIA AL LABORATORIO",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Período: " + String.format("%02d/%d", month, year)));
            document.add(Chunk.NEWLINE);

            int[] monthlyCounts = getAttendancesPerMonth(year);
            byte[] chartBytes = generateBarChart(monthlyCounts, year);
            Image chart = Image.getInstance(chartBytes);
            chart.scaleToFit(500, 300);
            document.add(chart);

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Detalle del mes:"));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell("Código");
            table.addCell("Nombre");
            table.addCell("Rol");
            table.addCell("Asistencias");

            for (AttendanceReportDTO d : data) {
                table.addCell(d.getUserCode());
                table.addCell(d.getUserName());
                table.addCell(d.getUserRole());
                table.addCell(String.valueOf(d.getTotalAttendances()));
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new SglException("Error generating PDF", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public byte[] generateAttendanceCsv(List<AttendanceReportDTO> data, int year) {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT
                .builder()
                .setHeader("Código", "Nombre", "Rol", "Total Asistencias")
                .build();

        try (CSVPrinter csv = new CSVPrinter(writer, format)) {

            for (AttendanceReportDTO d : data) {
                csv.printRecord(
                        d.getUserCode(),
                        d.getUserName(),
                        d.getUserRole(),
                        d.getTotalAttendances());
            }

            csv.println();
            csv.printComment("Resumen anual de asistencias");

            int[] monthlyCounts = getAttendancesPerMonth(year);

            csv.printRecord("Mes", "Asistencias");
            for (int i = 0; i < 12; i++) {
                csv.printRecord(i + 1, monthlyCounts[i]);
            }

        } catch (IOException e) {
            throw new SglException("Error generating CSV", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }

    public int[] getAttendancesPerMonth(int year) {
        int[] counts = new int[12];

        for (int month = 1; month <= 12; month++) {
            counts[month - 1] = attendanceRepository.countByYearAndMonth(year, month);
        }

        return counts;
    }

    private byte[] generateBarChart(int[] values, int year) throws IOException {
        int width = 800;
        int height = 400;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.BLACK);
        g.drawString("Asistencias por mes - " + year, 10, 20);

        int max = Arrays.stream(values).max().orElse(1);
        int barWidth = width / 14;

        for (int i = 0; i < 12; i++) {
            int barHeight = (int) ((double) values[i] / max * (height - 60));

            g.setColor(new Color(100, 149, 237)); 
            g.fillRect(40 + i * (barWidth + 15), height - barHeight - 30, barWidth, barHeight);

            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(i + 1), 45 + i * (barWidth + 15), height - 10);
            g.drawString(String.valueOf(values[i]), 45 + i * (barWidth + 15), height - barHeight - 40);
        }

        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }
}