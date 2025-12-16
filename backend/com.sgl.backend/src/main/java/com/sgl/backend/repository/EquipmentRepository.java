package com.sgl.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sgl.backend.entity.Equipment;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    Optional<Equipment> findByBarcode(String barcode);
}
