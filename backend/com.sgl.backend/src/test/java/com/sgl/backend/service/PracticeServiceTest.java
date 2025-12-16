package com.sgl.backend.service;

import com.sgl.backend.dto.PracticeRequest;
import com.sgl.backend.dto.PracticeResponse;
import com.sgl.backend.entity.*;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.repository.LaboratoryRepository;
import com.sgl.backend.repository.PracticeScheduleRepository;
import com.sgl.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PracticeServiceTest {

        @Mock
        private PracticeScheduleRepository scheduleRepo;
        @Mock
        private LaboratoryRepository labRepo;
        @Mock
        private UserRepository userRepo;

        @InjectMocks
        private PracticeService practiceService;

        @Test
        void schedulePractice_success_defaultLab() {
                PracticeRequest request = new PracticeRequest();
                request.setSubject("Basic Electronics");
                request.setPracticeType(PracticeType.ELECTRONICA);
                request.setDate(LocalDate.now().plusDays(1));
                request.setStartTime(LocalTime.of(10, 0));
                request.setDurationMinutes(90);
                request.setStudentCount(20);

                User teacher = User.builder().code("DOC123").build();
                Laboratory defaultLab = Laboratory.builder()
                                .id(1L)
                                .name("Laboratorio Principal")
                                .capacity(30)
                                .active(true)
                                .build();

                PracticeSchedule savedSchedule = PracticeSchedule.builder()
                                .id(1L)
                                .teacher(teacher)
                                .laboratory(defaultLab)
                                .practiceType(PracticeType.ELECTRONICA)
                                .subject("Basic Electronics")
                                .date(request.getDate())
                                .startTime(request.getStartTime())
                                .durationMinutes(90)
                                .endTime(request.getStartTime().plusMinutes(90))
                                .build();

                when(userRepo.findById("DOC123")).thenReturn(Optional.of(teacher));
                when(labRepo.findFirstByActiveTrue()).thenReturn(Optional.of(defaultLab));
                when(scheduleRepo.existsByLaboratoryAndDateAndStartTimeLessThanEqualAndStartTimeGreaterThanEqual(any(),
                                any(), any(), any()))
                                .thenReturn(false);
                when(scheduleRepo.existsByLaboratoryAndDateAndStartTimeLessThanAndEndTimeGreaterThan(any(), any(),
                                any(), any()))
                                .thenReturn(false);
                when(scheduleRepo.save(any())).thenReturn(savedSchedule);

                PracticeResponse response = practiceService.schedulePractice("DOC123", request);

                assertThat(response.getId()).isEqualTo(1L);
                assertThat(response.getLaboratoryName()).isEqualTo("Laboratorio Principal");
                assertThat(response.getDurationMinutes()).isEqualTo(90);
                assertThat(response.getEndTime()).isEqualTo(LocalTime.of(11, 30));
                verify(scheduleRepo).save(any());
        }

        @Test
        void schedulePractice_conflict_throwsException() {
                PracticeRequest request = new PracticeRequest();
                request.setPracticeType(PracticeType.ELECTRONICA);
                request.setDate(LocalDate.now());
                request.setStartTime(LocalTime.of(10, 0));
                request.setDurationMinutes(60);
                request.setStudentCount(10);

                Laboratory lab = Laboratory.builder().id(1L).name("Laboratorio Principal").capacity(18).active(true).build();

                when(userRepo.findById("DOC123")).thenReturn(Optional.of(User.builder().code("DOC123").build()));
                when(labRepo.findFirstByActiveTrue()).thenReturn(Optional.of(lab));
                when(scheduleRepo.existsByLaboratoryAndDateAndStartTimeLessThanEqualAndStartTimeGreaterThanEqual(any(),
                                any(), any(), any()))
                                .thenReturn(true);
                when(scheduleRepo.findByLaboratoryAndDate(any(), any()))
                                .thenReturn(List.of(
                                                PracticeSchedule.builder().startTime(LocalTime.of(10, 0))
                                                                .endTime(LocalTime.of(11, 0)).build()));

                SglException ex = assertThrows(SglException.class,
                                () -> practiceService.schedulePractice("DOC123", request));

                assertThat(ex.getMessage()).contains("already reserved");
        }

        @Test
        void schedulePractice_labNotFound_throwsException() {
                PracticeRequest request = new PracticeRequest();
                request.setLaboratoryName("Inexistent Lab");

                when(userRepo.findById("DOC123")).thenReturn(Optional.of(User.builder().code("DOC123").build()));
                when(labRepo.findByNameAndActive("Inexistent Lab", true)).thenReturn(Optional.empty());

                SglException ex = assertThrows(SglException.class,
                                () -> practiceService.schedulePractice("DOC123", request));

                assertThat(ex.getMessage()).contains("Laboratory not found or inactive");
        }

        @Test
        void schedulePractice_teacherNotFound_throwsException() {
                PracticeRequest request = new PracticeRequest();
                request.setSubject("Test");

                when(userRepo.findById("UNKNOWN")).thenReturn(Optional.empty());

                SglException ex = assertThrows(SglException.class,
                                () -> practiceService.schedulePractice("UNKNOWN", request));

                assertThat(ex.getMessage()).contains("Teacher not found");
        }

        @Test
        void schedulePractice_labCapacityExceeded_throwsException() {
                PracticeRequest request = new PracticeRequest();
                request.setSubject("Química I");
                request.setPracticeType(PracticeType.FISICA);
                request.setDate(LocalDate.now().plusDays(2));
                request.setStartTime(LocalTime.of(9, 0));
                request.setDurationMinutes(60);
                request.setStudentCount(50); 

                User teacher = User.builder().code("DOC789").build();
                Laboratory lab = Laboratory.builder()
                                .id(3L)
                                .name("Lab Química")
                                .capacity(40)
                                .active(true)
                                .build();

                when(userRepo.findById("DOC789")).thenReturn(Optional.of(teacher));
                when(labRepo.findFirstByActiveTrue()).thenReturn(Optional.of(lab));

                SglException ex = assertThrows(SglException.class,
                                () -> practiceService.schedulePractice("DOC789", request));

                assertThat(ex.getMessage())
                                .contains("cannot accommodate")
                                .contains("capacity: 40");
        }

        @Test
        void schedulePractice_noActiveLabs_throwsException() {
                PracticeRequest request = new PracticeRequest();
                request.setSubject("Physics");
                request.setPracticeType(PracticeType.FISICA);
                request.setDate(LocalDate.now().plusDays(1));
                request.setStartTime(LocalTime.of(8, 0));
                request.setDurationMinutes(90);

                User teacher = User.builder().code("DOC111").build();

                when(userRepo.findById("DOC111")).thenReturn(Optional.of(teacher));
                when(labRepo.findFirstByActiveTrue()).thenReturn(Optional.empty());

                SglException ex = assertThrows(SglException.class,
                                () -> practiceService.schedulePractice("DOC111", request));

                assertThat(ex.getMessage()).contains("No active laboratories available");
        }

        @Test
        void getMySchedules_returnsPage() {
                Laboratory lab = Laboratory.builder().name("Lab X").build();
                PracticeSchedule s1 = PracticeSchedule.builder()
                                .id(1L)
                                .subject("Electrónica I")
                                .practiceType(PracticeType.ELECTRONICA)
                                .laboratory(lab)
                                .durationMinutes(90)
                                .build();

                PracticeSchedule s2 = PracticeSchedule.builder()
                                .id(2L)
                                .subject("Física II")
                                .practiceType(PracticeType.FISICA)
                                .laboratory(lab)
                                .durationMinutes(120)
                                .build();

                Pageable pageable = PageRequest.of(0, 10);
                Page<PracticeSchedule> page = new PageImpl<>(List.of(s1, s2), pageable, 2);

                when(scheduleRepo.findByTeacherCode("DOC123", pageable)).thenReturn(page);

                Page<PracticeResponse> result = practiceService.getMySchedules("DOC123", pageable);

                assertThat(result).hasSize(2);
                assertThat(result.getContent().get(0).getSubject()).isEqualTo("Electrónica I");
                assertThat(result.getContent().get(1).getPracticeType()).isEqualTo(PracticeType.FISICA);
        }
}