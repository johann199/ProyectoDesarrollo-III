package com.sgl.backend.controller;

import com.sgl.backend.dto.LaboratoryRequest;
import com.sgl.backend.entity.Laboratory;
import com.sgl.backend.service.LaboratoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LaboratoryControllerTest {

    @Mock
    private LaboratoryService laboratoryService;

    @InjectMocks
    private LaboratoryController laboratoryController;

    @Test
    void createLaboratory_returnsCreatedLab() {
        LaboratoryRequest request = new LaboratoryRequest();
        request.setName("Lab Computación");
        request.setCapacity(25);

        Laboratory created = Laboratory.builder()
                .id(1L)
                .name("Lab Computación")
                .capacity(25)
                .active(true)
                .build();

        when(laboratoryService.create(any(LaboratoryRequest.class))).thenReturn(created);

        Laboratory result = laboratoryController.createLaboratory(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Lab Computación");
        assertThat(result.getCapacity()).isEqualTo(25);
        assertThat(result.isActive()).isTrue();

        verify(laboratoryService).create(any(LaboratoryRequest.class));
    }

    @Test
    void listActive_returnsPageOfLabs() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        Laboratory lab1 = Laboratory.builder().id(1L).name("Lab Física").active(true).build();
        Laboratory lab2 = Laboratory.builder().id(2L).name("Lab Electrónica").active(true).build();

        Page<Laboratory> labPage = new PageImpl<>(List.of(lab1, lab2), pageable, 2);

        when(laboratoryService.getActive(any(Pageable.class))).thenReturn(labPage);

        ResponseEntity<Page<Laboratory>> response = laboratoryController.listActive();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2);
        assertThat(response.getBody().getContent().get(0).getName()).isEqualTo("Lab Física");

        verify(laboratoryService).getActive(any(Pageable.class));
    }

    @Test
    void deactivate_callsServiceAndReturnsOk() {
        doNothing().when(laboratoryService).deactivate(5L);

        ResponseEntity<Void> response = laboratoryController.deactivate(5L);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(laboratoryService).deactivate(5L);
    }
}
