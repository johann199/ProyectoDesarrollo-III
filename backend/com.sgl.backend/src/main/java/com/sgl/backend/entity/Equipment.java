package com.sgl.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String barcode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer totalUnits;

    @Column(nullable = false)
    private Integer availableUnits;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;

    public enum EquipmentStatus {
        AVAILABLE,
        LOANED
    }
}
