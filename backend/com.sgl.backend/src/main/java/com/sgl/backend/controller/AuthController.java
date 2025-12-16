package com.sgl.backend.controller;

import com.sgl.backend.dto.LoginRequest;
import com.sgl.backend.dto.LoginResponse;
import com.sgl.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public class AuthController {
    
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates a user via SIRA (for students/teachers) or locally (for admin) and returns a JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful authentication, returns JWT and role"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}
