package com.sgl.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sgl.backend.entity.Laboratory;

@Repository
public interface LaboratoryRepository extends JpaRepository<Laboratory, Long> {
    Optional<Laboratory> findByNameAndActive(String name, boolean active);
    Optional<Laboratory> findFirstByActiveTrue();
    Page<Laboratory> findByActiveTrue(Pageable pageable);
}
