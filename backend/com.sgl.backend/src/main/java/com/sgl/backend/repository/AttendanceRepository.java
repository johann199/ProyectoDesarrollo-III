package com.sgl.backend.repository;

import com.sgl.backend.entity.Attendance;
import com.sgl.backend.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
        Optional<Attendance> findByUserAndTimestampBetween(User user, LocalDateTime start, LocalDateTime end);

        List<Attendance> findByUserCode(String userCode);

        List<Attendance> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

        @Query("""
                        SELECT a FROM Attendance a
                        WHERE YEAR(a.timestamp) = :year
                          AND MONTH(a.timestamp) = :month
                        ORDER BY a.timestamp
                        """)
        Page<Attendance> findByYearAndMonth(
                        @Param("year") int year,
                        @Param("month") int month,
                        Pageable pageable);

        @Query("""
                        SELECT COUNT(a) FROM Attendance a
                        WHERE a.user.code = :userCode
                          AND YEAR(a.timestamp) = :year
                          AND MONTH(a.timestamp) = :month
                        """)
        long countByUserAndYearMonth(@Param("userCode") String userCode,
                        @Param("year") int year,
                        @Param("month") int month);

        @Query("SELECT COUNT(a) FROM Attendance a WHERE YEAR(a.timestamp) = :year AND MONTH(a.timestamp) = :month")
        int countByYearAndMonth(@Param("year") int year, @Param("month") int month);
}
