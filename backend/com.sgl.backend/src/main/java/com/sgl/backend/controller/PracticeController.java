package com.sgl.backend.controller;

import com.sgl.backend.dto.PracticeRequest;
import com.sgl.backend.dto.PracticeResponse;
import com.sgl.backend.service.PracticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/practices")
@RequiredArgsConstructor
@Tag(name = "Practice Scheduling", description = "Endpoints for scheduling and managing practice sessions")
public class PracticeController {
    private final PracticeService practiceService;

    @PostMapping
    @Operation(summary = "Schedule a Practice Session", description = "Schedules a new practice session for a teacher in a specified laboratory.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Practice session scheduled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Only teachers can schedule practice sessions"),
            @ApiResponse(responseCode = "404", description = "Teacher or laboratory not found"),
    })
    public ResponseEntity<PracticeResponse> schedule(
            @Valid @RequestBody PracticeRequest request,
            Authentication authentication) {
        String teacherCode = (String) authentication.getPrincipal();
        return ResponseEntity.ok(practiceService.schedulePractice(teacherCode, request));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<PracticeResponse>> getMySchedules(Authentication authentication) {
        String teacherCode = (String) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date"));
        Page<PracticeResponse> page = practiceService.getMySchedules(teacherCode, pageable);
        return ResponseEntity.ok(page);
    }
}
