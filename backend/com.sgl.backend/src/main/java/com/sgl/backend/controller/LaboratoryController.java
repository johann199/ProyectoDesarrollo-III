package com.sgl.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sgl.backend.dto.LaboratoryRequest;
import com.sgl.backend.entity.Laboratory;
import com.sgl.backend.service.LaboratoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/laboratories")
@RequiredArgsConstructor
@Tag(name = "Laboratory", description = "Endpoints for managing laboratories")
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    @PostMapping
    @Operation(summary = "Create a new laboratory", description = "Creates a new laboratory with the provided details.")
    public Laboratory createLaboratory(@RequestBody LaboratoryRequest request) {
        return laboratoryService.create(request);
    }

    @GetMapping
    @Operation(summary = "Get active laboratories", description = "Retrieves a paginated list of active laboratories.")
    public ResponseEntity<Page<Laboratory>> listActive() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(laboratoryService.getActive(pageable));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a laboratory", description = "Deactivates the laboratory with the specified ID.")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        laboratoryService.deactivate(id);
        return ResponseEntity.ok().build();
    }
}
