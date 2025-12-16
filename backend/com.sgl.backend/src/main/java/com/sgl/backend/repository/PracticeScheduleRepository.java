package com.sgl.backend.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sgl.backend.entity.Laboratory;
import com.sgl.backend.entity.PracticeSchedule;

@Repository
public interface PracticeScheduleRepository extends JpaRepository<PracticeSchedule, Long> {
        Page<PracticeSchedule> findByTeacherCode(String teacherCode, Pageable pageable);

        boolean existsByLaboratoryAndDateAndStartTimeLessThanEqualAndStartTimeGreaterThanEqual(
                        Laboratory lab, LocalDate date, LocalTime end, LocalTime start);

        boolean existsByLaboratoryAndDateAndStartTimeLessThanAndEndTimeGreaterThan(
                        Laboratory lab, LocalDate date, LocalTime start, LocalTime end);


        @Query("SELECT ps FROM PracticeSchedule ps WHERE ps.laboratory = :lab AND ps.date = :date ORDER BY ps.startTime")
        List<PracticeSchedule> findByLaboratoryAndDate(
                @Param("lab") Laboratory lab,
                @Param("date") LocalDate date
        );
}
