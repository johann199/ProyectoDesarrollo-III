package com.sgl.backend.service;

import com.sgl.backend.dto.LaboratoryRequest;
import com.sgl.backend.entity.Laboratory;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.repository.LaboratoryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LaboratoryServiceTest {

    @Mock
    private LaboratoryRepository labRepo;

    @InjectMocks
    private LaboratoryService labService;

    @Test
    void create_successfullyCreatesLaboratory() {
        LaboratoryRequest request = new LaboratoryRequest();
        request.setName("Lab Fisica");
        request.setCapacity(40);

        when(labRepo.findByNameAndActive("Lab Fisica", true)).thenReturn(Optional.empty());
        when(labRepo.save(any(Laboratory.class))).thenAnswer(invocation -> {
            Laboratory saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Laboratory result = labService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Lab Fisica");
        assertThat(result.isActive()).isTrue();

        verify(labRepo).findByNameAndActive("Lab Fisica", true);
        verify(labRepo).save(any(Laboratory.class));
    }

    @Test
    void create_duplicateName_throwsConflictException() {
        LaboratoryRequest request = new LaboratoryRequest();
        request.setName("Lab Quimica");
        request.setCapacity(25);

        when(labRepo.findByNameAndActive("Lab Quimica", true))
                .thenReturn(Optional.of(Laboratory.builder().id(2L).name("Lab Quimica").active(true).build()));

        SglException ex = assertThrows(SglException.class, () -> labService.create(request));

        assertThat(ex.getMessage()).contains("already exists");
        assertThat(ex.getStatus()).isNotNull();
        assertThat(ex.getStatus().value()).isEqualTo(409); 

        verify(labRepo, never()).save(any());
    }

    @Test
    void getActive_returnsActiveLaboratoriesPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        Laboratory lab1 = Laboratory.builder().id(1L).name("Lab Fisica").active(true).build();
        Laboratory lab2 = Laboratory.builder().id(2L).name("Lab Electronica").active(true).build();
        Page<Laboratory> page = new PageImpl<>(List.of(lab1, lab2));

        when(labRepo.findByActiveTrue(pageable)).thenReturn(page);

        Page<Laboratory> result = labService.getActive(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Lab Fisica");

        verify(labRepo).findByActiveTrue(pageable);
    }

    @Test
    void deactivate_existingLab_setsInactive() {
        Laboratory lab = Laboratory.builder()
                .id(5L)
                .name("Lab Computación")
                .capacity(20)
                .active(true)
                .build();

        when(labRepo.findById(5L)).thenReturn(Optional.of(lab));
        when(labRepo.save(any(Laboratory.class))).thenReturn(lab);

        labService.deactivate(5L);

        assertThat(lab.isActive()).isFalse();
        verify(labRepo).findById(5L);
        verify(labRepo).save(lab);
    }

    @Test
    void deactivate_notFound_throwsNotFoundException() {
        when(labRepo.findById(99L)).thenReturn(Optional.empty());

        SglException ex = assertThrows(SglException.class, () -> labService.deactivate(99L));

        assertThat(ex.getMessage()).contains("not found");
        assertThat(ex.getStatus().value()).isEqualTo(404); 

        verify(labRepo, never()).save(any());
    }
}
