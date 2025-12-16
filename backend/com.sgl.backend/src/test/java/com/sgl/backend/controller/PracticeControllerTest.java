package com.sgl.backend.controller;

import com.sgl.backend.dto.PracticeRequest;
import com.sgl.backend.dto.PracticeResponse;
import com.sgl.backend.entity.PracticeType;
import com.sgl.backend.service.PracticeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PracticeControllerTest {

        @Mock
        private PracticeService practiceService;

        @Mock
        private Authentication authentication;

        @InjectMocks
        private PracticeController practiceController;

        @Test
        void schedule_returnsOkResponse() {
                when(authentication.getPrincipal()).thenReturn("DOC123");

                PracticeRequest request = new PracticeRequest();
                request.setSubject("Física Cuántica");
                request.setPracticeType(PracticeType.FISICA);
                request.setDate(LocalDate.now().plusDays(1));
                request.setStartTime(LocalTime.of(9, 0));
                request.setDurationMinutes(90);
                request.setStudentCount(15);

                PracticeResponse saved = PracticeResponse.builder()
                                .id(1L)
                                .subject("Física Cuántica")
                                .practiceType(PracticeType.FISICA)
                                .laboratoryName("Lab 101")
                                .build();

                when(practiceService.schedulePractice(eq("DOC123"), any(PracticeRequest.class)))
                                .thenReturn(saved);

                ResponseEntity<PracticeResponse> response = practiceController.schedule(request, authentication);

                assertThat(response.getStatusCode().value()).isEqualTo(200);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getId()).isEqualTo(1L);
                assertThat(response.getBody().getSubject()).isEqualTo("Física Cuántica");

                verify(practiceService).schedulePractice(eq("DOC123"), any(PracticeRequest.class));
        }

        @Test
        void getMySchedules_returnsPagedResponse() {
                when(authentication.getPrincipal()).thenReturn("DOC123");

                PracticeResponse r1 = PracticeResponse.builder()
                                .id(1L)
                                .subject("Electrónica I")
                                .practiceType(PracticeType.ELECTRONICA)
                                .laboratoryName("Lab 1")
                                .build();

                PracticeResponse r2 = PracticeResponse.builder()
                                .id(2L)
                                .subject("Física II")
                                .practiceType(PracticeType.FISICA)
                                .laboratoryName("Lab 2")
                                .build();

                Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date"));
                Page<PracticeResponse> page = new PageImpl<>(List.of(r1, r2), pageable, 2);

                when(practiceService.getMySchedules(eq("DOC123"), any(Pageable.class))).thenReturn(page);

                ResponseEntity<Page<PracticeResponse>> response = practiceController.getMySchedules(authentication);

                assertThat(response.getStatusCode().value()).isEqualTo(200);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getTotalElements()).isEqualTo(2);
                assertThat(response.getBody().getContent().get(0).getSubject()).isEqualTo("Electrónica I");
                assertThat(response.getBody().getContent().get(1).getLaboratoryName()).isEqualTo("Lab 2");

                verify(practiceService).getMySchedules(eq("DOC123"), any(Pageable.class));
        }
}
