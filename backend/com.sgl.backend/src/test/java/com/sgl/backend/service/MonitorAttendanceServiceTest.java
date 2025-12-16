package com.sgl.backend.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import com.sgl.backend.dto.AttendanceResponse;
import com.sgl.backend.dto.MonitorReportResponse;
import com.sgl.backend.dto.AttendanceRequest;
import com.sgl.backend.entity.MonitorAttendance;
import com.sgl.backend.entity.Role;
import com.sgl.backend.entity.User;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.repository.MonitorAttendanceRepository;
import com.sgl.backend.repository.RoleRepository;
import com.sgl.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

class MonitorAttendanceServiceTest {

        @Mock
        private MonitorAttendanceRepository attendanceRepo;

        @Mock
        private UserRepository userRepo;

        @Mock
        private RoleRepository roleRepo;

        @InjectMocks
        private MonitorAttendanceService service;

        private User monitor;

        @BeforeEach
        void setup() {
                MockitoAnnotations.openMocks(this);

                Role role = new Role();
                role.setName("MONITOR");

                monitor = new User();
                monitor.setCode("M001");
                monitor.setName("Juan Pérez");
                monitor.setRole(role);
        }

        @Test
        void testCheckInSuccessWhenNoPreviousRecord() {
                AttendanceRequest req = new AttendanceRequest();
                req.setMonitorCode("M001");
                req.setType(AttendanceRequest.AttendanceType.CHECK_IN);

                when(userRepo.findById("M001")).thenReturn(Optional.of(monitor));
                when(attendanceRepo.findFirstByMonitorCodeAndDateOrderByCheckInDesc(eq("M001"), any()))
                                .thenReturn(Optional.empty());

                AttendanceResponse resp = service.registerAttendance(req);

                assertNotNull(resp.getCheckIn());
                assertNull(resp.getCheckOut());
                assertEquals("IN_PROGRESS", resp.getStatus());
        }

        @Test
        void testCheckInFailsWhenLastRecordHasNoCheckOut() {
                AttendanceRequest req = new AttendanceRequest();
                req.setMonitorCode("M001");
                req.setType(AttendanceRequest.AttendanceType.CHECK_IN);

                MonitorAttendance last = MonitorAttendance.builder()
                                .monitor(monitor)
                                .date(LocalDate.now())
                                .checkIn(LocalDateTime.now())
                                .build();

                when(userRepo.findById("M001")).thenReturn(Optional.of(monitor));
                when(attendanceRepo.findFirstByMonitorCodeAndDateOrderByCheckInDesc(eq("M001"), any()))
                                .thenReturn(Optional.of(last));

                assertThrows(SglException.class, () -> service.registerAttendance(req));
        }

        @Test
        void testCheckOutSuccess() {
                AttendanceRequest req = new AttendanceRequest();
                req.setMonitorCode("M001");
                req.setType(AttendanceRequest.AttendanceType.CHECK_OUT);

                MonitorAttendance last = MonitorAttendance.builder()
                                .monitor(monitor)
                                .date(LocalDate.now())
                                .checkIn(LocalDateTime.now().minusHours(2))
                                .build();

                when(userRepo.findById("M001")).thenReturn(Optional.of(monitor));
                when(attendanceRepo.findFirstByMonitorCodeAndDateOrderByCheckInDesc(eq("M001"), any()))
                                .thenReturn(Optional.of(last));

                AttendanceResponse resp = service.registerAttendance(req);

                assertNotNull(resp.getCheckOut());
                assertEquals("COMPLETED", resp.getStatus());
                assertTrue(resp.getHoursWorked() > 1.9);
        }

        @Test
        void testCheckoutFailsWithoutCheckIn() {
                AttendanceRequest req = new AttendanceRequest();
                req.setMonitorCode("M001");
                req.setType(AttendanceRequest.AttendanceType.CHECK_OUT);

                when(userRepo.findById("M001")).thenReturn(Optional.of(monitor));
                when(attendanceRepo.findFirstByMonitorCodeAndDateOrderByCheckInDesc(any(), any()))
                                .thenReturn(Optional.empty());

                assertThrows(SglException.class, () -> service.registerAttendance(req));
        }

        @Test
        void testValidateMonitorFailsWhenNotFound() {
                AttendanceRequest req = new AttendanceRequest();
                req.setMonitorCode("X001");
                req.setType(AttendanceRequest.AttendanceType.CHECK_IN);

                when(userRepo.findById("X001")).thenReturn(Optional.empty());

                assertThrows(SglException.class, () -> service.registerAttendance(req));
        }

        @Test
        void testValidateMonitorFailsWhenRoleIsNotMonitor() {
                AttendanceRequest req = new AttendanceRequest();
                req.setMonitorCode("A001");
                req.setType(AttendanceRequest.AttendanceType.CHECK_IN);

                User wrong = new User();
                Role role = new Role();
                role.setName("ADMIN");
                wrong.setRole(role);

                when(userRepo.findById("A001")).thenReturn(Optional.of(wrong));

                assertThrows(SglException.class, () -> service.registerAttendance(req));
        }

        @Test
        void testGetReportSuccessful() {
                LocalDate start = LocalDate.now().minusDays(5);
                LocalDate end = LocalDate.now();

                Role role = new Role();
                role.setName("MONITOR");

                when(roleRepo.findByName("MONITOR")).thenReturn(Optional.of(role));
                when(userRepo.findByRole(eq(role), any(Pageable.class))).thenReturn(
                                new PageImpl<>(List.of(monitor)));

                MonitorAttendance r1 = MonitorAttendance.builder()
                                .monitor(monitor)
                                .date(LocalDate.now())
                                .checkIn(LocalDateTime.now().minusHours(2))
                                .checkOut(LocalDateTime.now())
                                .build();

                when(attendanceRepo.findByMonitorCodeAndDateBetween(eq("M001"), eq(start), eq(end)))
                                .thenReturn(List.of(r1));

                Page<MonitorReportResponse> page = service.getReport(start, end, PageRequest.of(0, 10));

                assertEquals(1, page.getContent().size());

                MonitorReportResponse rep = page.getContent().get(0);

                assertEquals("M001", rep.getMonitorCode());
                assertEquals(1, rep.getTotalDaysWorked());
                assertTrue(rep.getTotalHoursWorked() >= 1.9);
        }

        @Test
        void testGetReportFailsWhenMonitorRoleMissing() {
                when(roleRepo.findByName("MONITOR")).thenReturn(Optional.empty());

                assertThrows(SglException.class,
                                () -> service.getReport(LocalDate.now(), LocalDate.now(), PageRequest.of(0, 10)));
        }

        @Test
        void testGenerateCsvProducesContent() {
                MonitorReportResponse m = MonitorReportResponse.builder()
                                .monitorCode("M001")
                                .monitorName("Juan Pérez")
                                .totalDaysWorked(3)
                                .totalHoursWorked(10.5)
                                .build();

                byte[] csv = service.generateMonitorReportCsv(List.of(m));

                String content = new String(csv, StandardCharsets.UTF_8);

                System.out.println("--- CSV generado ---");
                System.out.println(content);
                System.out.println("--------------------");

                assertTrue(content.contains("Código") || content.contains("\"Código\""),
                                "Debe contener encabezado Código");

                assertTrue(content.contains("M001") || content.contains("\"M001\""),
                                "Debe contener código del monitor");
        }

        @Test
        void testGeneratePdfReturnsBytes() {
                MonitorReportResponse m = MonitorReportResponse.builder()
                                .monitorCode("M001")
                                .monitorName("Juan Pérez")
                                .totalDaysWorked(3)
                                .totalHoursWorked(5.5)
                                .build();

                byte[] pdf = service.generateMonitorReportPdf(List.of(m),
                                LocalDate.now().minusDays(3),
                                LocalDate.now());

                assertNotNull(pdf);
                assertTrue(pdf.length > 100);
        }
}
