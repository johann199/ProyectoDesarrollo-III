package com.sgl.backend.controller;

import com.sgl.backend.entity.User;
import com.sgl.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing user roles")
public class UserController {
    
    private final UserService userService;

    @PutMapping("/{userCode}/role")
    @Operation(summary = "Update user role", description = "Assigns a new role to a user. Restricted to ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully"),
            @ApiResponse(responseCode = "400", description = "User or role not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Requires ADMIN role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<User> updateUserRole(@PathVariable String userCode, @RequestParam String roleName) {
        User updatedUser = userService.updateUserRole(userCode, roleName);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping
    @Operation(summary = "List all users", description = "Retrieves all users with their roles for admin management. Restricted to ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Requires ADMIN role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
