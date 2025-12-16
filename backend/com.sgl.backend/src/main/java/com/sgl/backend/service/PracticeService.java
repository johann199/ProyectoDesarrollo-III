package com.sgl.backend.service;

import com.sgl.backend.dto.PracticeRequest;
import com.sgl.backend.dto.PracticeResponse;
import com.sgl.backend.entity.*;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.repository.LaboratoryRepository;
import com.sgl.backend.repository.PracticeScheduleRepository;
import com.sgl.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PracticeService {

        private final PracticeScheduleRepository scheduleRepo;
        private final LaboratoryRepository labRepo;
        private final UserRepository userRepo;

        @Transactional
        public PracticeResponse schedulePractice(String teacherCode, PracticeRequest request) {
                User teacher = userRepo.findById(teacherCode)
                                .orElseThrow(() -> new SglException("Teacher not found", HttpStatus.NOT_FOUND));

                Laboratory lab = resolveLaboratory(request.getLaboratoryName());

                if (request.getStudentCount() > lab.getCapacity()) {
                        throw new SglException(
                                        "The laboratory '" + lab.getName() + "' cannot accommodate "
                                                        + request.getStudentCount()
                                                        + " students (capacity: " + lab.getCapacity() + ")",
                                        HttpStatus.UNPROCESSABLE_ENTITY);
                }

                LocalTime endTime = request.getStartTime().plusMinutes(request.getDurationMinutes());

                boolean conflict = scheduleRepo
                                .existsByLaboratoryAndDateAndStartTimeLessThanEqualAndStartTimeGreaterThanEqual(
                                                lab, request.getDate(), endTime, request.getStartTime())
                                || scheduleRepo.existsByLaboratoryAndDateAndStartTimeLessThanAndEndTimeGreaterThan(
                                                lab, request.getDate(), request.getStartTime(), endTime);

                if (conflict) {
                        List<PracticeSchedule> occupied = scheduleRepo.findByLaboratoryAndDate(lab, request.getDate());
                        
                        String occupiedRanges = occupied.stream()
                                        .map(s -> s.getStartTime() + " - " + s.getEndTime())
                                        .sorted()
                                        .collect(Collectors.joining(", "));
                        throw new SglException(
                                        "The laboratory '" + lab.getName() + "' is already reserved at that time: " + occupiedRanges,
                                        HttpStatus.CONFLICT);
                }

                PracticeSchedule saved = scheduleRepo.save(
                                PracticeSchedule.builder()
                                                .teacher(teacher)
                                                .laboratory(lab)
                                                .practiceType(request.getPracticeType())
                                                .subject(request.getSubject())
                                                .date(request.getDate())
                                                .startTime(request.getStartTime())
                                                .durationMinutes(request.getDurationMinutes())
                                                .endTime(endTime)
                                                .studentCount(request.getStudentCount())
                                                .build());

                return PracticeResponse.builder()
                                .id(saved.getId())
                                .subject(saved.getSubject())
                                .practiceType(saved.getPracticeType())
                                .date(saved.getDate())
                                .startTime(saved.getStartTime())
                                .endTime(saved.getEndTime())
                                .durationMinutes(saved.getDurationMinutes())
                                .laboratoryName(saved.getLaboratory().getName())
                                .studentCount(saved.getStudentCount())
                                .build();
        }

        private Laboratory resolveLaboratory(String name) {
                if (name != null && !name.isBlank()) {
                        return labRepo.findByNameAndActive(name, true)
                                        .orElseThrow(() -> new SglException(
                                                        "Laboratory not found or inactive: " + name,
                                                        HttpStatus.NOT_FOUND));
                }

                return labRepo.findFirstByActiveTrue()
                                .orElseThrow(() -> new SglException(
                                                "No active laboratories available",
                                                HttpStatus.UNPROCESSABLE_ENTITY));
        }

        public Page<PracticeResponse> getMySchedules(String teacherCode, Pageable pageable) {
                Page<PracticeSchedule> schedules = scheduleRepo.findByTeacherCode(teacherCode, pageable);

                return schedules.map(schedule -> PracticeResponse.builder()
                                .id(schedule.getId())
                                .subject(schedule.getSubject())
                                .practiceType(schedule.getPracticeType())
                                .date(schedule.getDate())
                                .startTime(schedule.getStartTime())
                                .endTime(schedule.getEndTime())
                                .durationMinutes(schedule.getDurationMinutes())
                                .laboratoryName(schedule.getLaboratory().getName())
                                .studentCount(schedule.getStudentCount())
                                .build());
        }
}
