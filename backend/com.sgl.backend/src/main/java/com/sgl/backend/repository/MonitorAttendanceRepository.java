package com.sgl.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sgl.backend.entity.MonitorAttendance;

public interface MonitorAttendanceRepository extends JpaRepository<MonitorAttendance, Long> {
    Optional<MonitorAttendance> findByMonitorCodeAndDate(String monitorCode, LocalDate date);

    List<MonitorAttendance> findByMonitorCodeAndDateBetween(String monitorCode, LocalDate start, LocalDate end);

    List<MonitorAttendance> findByDateBetween(LocalDate start, LocalDate end);

    Optional<MonitorAttendance> findFirstByMonitorCodeAndDateOrderByCheckInDesc(String monitorCode, LocalDate date);
}
