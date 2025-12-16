package com.sgl.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sgl.backend.dto.LaboratoryRequest;
import com.sgl.backend.entity.Laboratory;
import com.sgl.backend.exception.SglException;
import com.sgl.backend.repository.LaboratoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LaboratoryService {

    private final LaboratoryRepository labRepo;

    public Laboratory create(LaboratoryRequest request) {
        if (labRepo.findByNameAndActive(request.getName(), true).isPresent()) {
            throw new SglException("A laboratory with the name '" + request.getName() + "' already exists.", HttpStatus.CONFLICT);
        }

        Laboratory lab = Laboratory.builder()
                .name(request.getName())
                .capacity(request.getCapacity())
                .active(true)
                .build();

        return labRepo.save(lab);
    }
    
    public Page<Laboratory> getActive(Pageable pageable) {
        return labRepo.findByActiveTrue(pageable);
    }

    public void deactivate(Long id) {
        Laboratory lab = labRepo.findById(id)
                .orElseThrow(() -> new SglException("Laboratory with id '" + id + "' not found.", HttpStatus.NOT_FOUND));

        lab.setActive(false);
        labRepo.save(lab);
    }
}
