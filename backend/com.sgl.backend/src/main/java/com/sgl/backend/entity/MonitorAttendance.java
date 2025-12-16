package com.sgl.backend.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "monitor:attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonitorAttendance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "monitor_code", nullable = false)
    private User monitor;

    @Column(nullable = false)
    private LocalDate date;

    private LocalDateTime checkIn;

    private LocalDateTime checkOut;

    @Transient
    private Double hoursWorked;
}
